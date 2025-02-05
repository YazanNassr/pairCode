import apiClient, { apiBaseUrl } from "./client.ts";
import { Project } from "../../types/types.ts";

const projectBaseUrl = () => `${apiBaseUrl()}/project`;

/** Lists projects on the user's personal project list. */
export async function getProjects(): Promise<Project[]> {
    const response = await apiClient.get(`${projectBaseUrl()}/all`);
    return response.data;
}

/** Fetches a project by id (link-based access). */
export async function getProject(projectId: string): Promise<Project> {
    const response = await apiClient.get(`${projectBaseUrl()}/${projectId}`);
    return response.data;
}

/** Removes a project from the list; owners hard-delete. */
export async function deleteProject(projectId: string): Promise<void> {
    await apiClient.delete(`${projectBaseUrl()}/${projectId}`);
}

/** Payload for creating a new project (id and ownerId are assigned server-side). */
export type CreateProjectPayload = Pick<Project, "name" | "files">;

/** Creates a new project. */
export async function createProject(project: CreateProjectPayload): Promise<Project> {
    const response = await apiClient.post(`${projectBaseUrl()}`, project);
    return response.data;
}

/** Runs the project entry file with stdin input. */
export async function runProject(
    projectId: string,
    input: string,
    mainFilePath: string,
    language: string
): Promise<string> {
    const response = await apiClient.post(`${projectBaseUrl()}/${projectId}/run`, {
        mainFilePath,
        input,
        language,
    });
    return response.data;
}
