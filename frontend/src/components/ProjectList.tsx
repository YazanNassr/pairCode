import Box from "@mui/material/Box";
import ProjectCard from "./ui/ProjectCard.tsx";
import { useQuery } from "@tanstack/react-query";
import { Project } from "../types/types.ts";
import { getProjects } from "../services/api/projectApi.ts";
import Typography from "@mui/material/Typography";

export default function ProjectList() {
    const { data, error, isSuccess } = useQuery<Project[], Error>({
        queryKey: ["projects"],
        queryFn: getProjects,
    });

    return (
        <Box
            sx={{
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
            }}
        >
            {isSuccess &&
                data.map((project) => (
                    <ProjectCard
                        key={project.id}
                        projectId={project.id ?? ""}
                        name={project.name}
                        ownerId={project.ownerId}
                    />
                ))}
            {error && <Typography variant={"h6"}>An Error Occurred...</Typography>}
        </Box>
    );
}
