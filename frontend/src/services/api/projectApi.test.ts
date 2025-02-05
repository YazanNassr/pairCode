import { describe, expect, it, vi, beforeEach } from "vitest";
import { getProjects, getProject, deleteProject, createProject, runProject } from "./projectApi.ts";

vi.mock("./client.ts", () => ({
    default: {
        get: vi.fn(),
        post: vi.fn(),
        delete: vi.fn(),
    },
    apiBaseUrl: () => "http://localhost:8080",
}));

import apiClient from "./client.ts";

describe("projectApi", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("getProjects calls list endpoint", async () => {
        vi.mocked(apiClient.get).mockResolvedValue({ data: [] });

        await getProjects();

        expect(apiClient.get).toHaveBeenCalledWith("http://localhost:8080/project/all");
    });

    it("getProject calls project by id", async () => {
        vi.mocked(apiClient.get).mockResolvedValue({ data: { id: "p1" } });

        await getProject("p1");

        expect(apiClient.get).toHaveBeenCalledWith("http://localhost:8080/project/p1");
    });

    it("deleteProject calls delete endpoint", async () => {
        vi.mocked(apiClient.delete).mockResolvedValue({});

        await deleteProject("p1");

        expect(apiClient.delete).toHaveBeenCalledWith("http://localhost:8080/project/p1");
    });

    it("createProject posts project payload", async () => {
        const project = { name: "New", files: [] };
        vi.mocked(apiClient.post).mockResolvedValue({ data: { id: "p1", ...project, ownerId: "owner" } });

        await createProject(project);

        expect(apiClient.post).toHaveBeenCalledWith("http://localhost:8080/project", project);
    });

    it("runProject posts run payload", async () => {
        vi.mocked(apiClient.post).mockResolvedValue({ data: "ok" });

        await runProject("p1", "stdin", "./main.py", "python");

        expect(apiClient.post).toHaveBeenCalledWith("http://localhost:8080/project/p1/run", {
            mainFilePath: "./main.py",
            input: "stdin",
            language: "python",
        });
    });
});
