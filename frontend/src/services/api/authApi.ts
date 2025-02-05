import apiClient, { apiBaseUrl } from "./client.ts";
import { User } from "../../types/types.ts";

/**
 * Registers a new user account.
 */
export async function register(user: User): Promise<void> {
    await apiClient.post(`${apiBaseUrl()}/register`, user);
}

/**
 * Logs in and stores JWT and username in session storage.
 */
export async function login(user: User): Promise<void> {
    const response = await apiClient.post(`${apiBaseUrl()}/login`, user);
    const jwtToken =
        response.headers.authorization ??
        response.headers.Authorization ??
        response.headers["authorization"];
    if (jwtToken) {
        sessionStorage.setItem("username", user.username);
        sessionStorage.setItem("jwt", jwtToken);
    } else {
        throw new Error("Login succeeded but no Authorization header was returned");
    }
}

/**
 * Clears local session credentials.
 */
export function logout(): void {
    sessionStorage.removeItem("jwt");
    sessionStorage.removeItem("username");
}
