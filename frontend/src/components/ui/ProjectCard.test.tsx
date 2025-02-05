import { describe, expect, it, vi, beforeEach } from "vitest";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import ProjectCard from "./ProjectCard.tsx";

vi.mock("../../services/api/projectApi.ts", () => ({
    deleteProject: vi.fn(() => Promise.resolve()),
}));

import { deleteProject } from "../../services/api/projectApi.ts";

function renderCard(props: { projectId: string; name: string; ownerId: string }) {
    const queryClient = new QueryClient();
    return render(
        <QueryClientProvider client={queryClient}>
            <MemoryRouter>
                <ProjectCard {...props} />
            </MemoryRouter>
        </QueryClientProvider>
    );
}

describe("ProjectCard", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        sessionStorage.setItem("username", "owner");
        vi.spyOn(window, "confirm").mockReturnValue(true);
    });

    it("shows owner delete confirmation", async () => {
        renderCard({ projectId: "p1", name: "Demo", ownerId: "owner" });

        fireEvent.click(screen.getByTitle("Delete project"));

        expect(window.confirm).toHaveBeenCalledWith('Delete "Demo" for everyone? This cannot be undone.');
        await waitFor(() => expect(deleteProject).toHaveBeenCalledWith("p1"));
    });

    it("shows guest remove confirmation", () => {
        sessionStorage.setItem("username", "guest");
        renderCard({ projectId: "p1", name: "Demo", ownerId: "owner" });

        fireEvent.click(screen.getByTitle("Remove from list"));

        expect(window.confirm).toHaveBeenCalledWith('Remove "Demo" from your project list?');
    });
});
