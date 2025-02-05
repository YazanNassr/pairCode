import { describe, expect, it, vi, beforeEach } from "vitest";
import { login, register, logout } from "./authApi.ts";

vi.mock("./client.ts", () => ({
    default: {
        post: vi.fn(),
    },
    apiBaseUrl: () => "http://localhost:8080",
}));

import apiClient from "./client.ts";

describe("authApi", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        sessionStorage.clear();
    });

    it("register posts credentials", async () => {
        vi.mocked(apiClient.post).mockResolvedValue({});

        await register({ username: "alice", password: "secret" });

        expect(apiClient.post).toHaveBeenCalledWith("http://localhost:8080/register", {
            username: "alice",
            password: "secret",
        });
    });

    it("login stores jwt and username from response header", async () => {
        vi.mocked(apiClient.post).mockResolvedValue({
            headers: { authorization: "Bearer token-123" },
        });

        await login({ username: "alice", password: "secret" });

        expect(sessionStorage.getItem("username")).toBe("alice");
        expect(sessionStorage.getItem("jwt")).toBe("Bearer token-123");
    });

    it("login throws when authorization header is missing", async () => {
        vi.mocked(apiClient.post).mockResolvedValue({ headers: {} });

        await expect(login({ username: "alice", password: "secret" })).rejects.toThrow(
            "Login succeeded but no Authorization header was returned"
        );
    });

    it("logout clears session storage", () => {
        sessionStorage.setItem("jwt", "token");
        sessionStorage.setItem("username", "alice");

        logout();

        expect(sessionStorage.getItem("jwt")).toBeNull();
        expect(sessionStorage.getItem("username")).toBeNull();
    });
});
