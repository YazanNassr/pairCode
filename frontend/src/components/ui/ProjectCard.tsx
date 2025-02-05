import { Paper } from "@mui/material";
import Typography from "@mui/material/Typography";
import DeleteIcon from "@mui/icons-material/Delete";
import ShareIcon from "@mui/icons-material/Share";
import Box from "@mui/material/Box";
import IconButton from "@mui/material/IconButton";
import { deleteProject } from "../../services/api/projectApi.ts";
import { toast } from "react-toastify";
import { useNavigate } from "react-router-dom";
import { useQueryClient } from "@tanstack/react-query";

type ProjectCardProps = {
    projectId: string;
    name: string;
    ownerId: string;
};

export default function ProjectCard({ projectId, name, ownerId }: ProjectCardProps) {
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const username = sessionStorage.getItem("username") ?? "";
    const isOwner = username === ownerId;

    const handleDelete = () => {
        const message = isOwner
            ? `Delete "${name}" for everyone? This cannot be undone.`
            : `Remove "${name}" from your project list?`;
        if (!window.confirm(message)) {
            return;
        }

        deleteProject(projectId)
            .then(() => {
                toast.info(isOwner ? "Project deleted." : "Removed from your list.");
                queryClient.invalidateQueries({ queryKey: ["projects"] });
            })
            .catch(() => toast.error("Could not update project list."));
    };

    const handleShare = () => {
        navigator.clipboard.writeText(`${window.location.origin}/editor/${projectId}`).then(() =>
            toast.info(`Link to "${name}" copied!`, {
                position: "bottom-center",
                autoClose: 1200,
                hideProgressBar: true,
                closeOnClick: true,
                pauseOnHover: true,
                draggable: true,
                theme: "colored",
            })
        );
    };

    const handleRowClick = () => {
        navigate(`/editor/${projectId}`);
    };

    return (
        <Paper
            variant="outlined"
            sx={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                mt: 1.5,
                px: 3,
                width: "95%",
                bgcolor: "primary.light",
                cursor: "pointer",
            }}
            onClick={handleRowClick}
        >
            <Typography
                sx={{
                    ml: 2,
                    fontSize: "1.5rem",
                    fontFamily: "bold",
                    textDecoration: "none",
                    color: "primary.contrastText",
                }}
            >
                {name}
            </Typography>

            <Box>
                <IconButton onClick={(e) => { e.stopPropagation(); handleShare(); }}>
                    <ShareIcon sx={{ m: 0.7, color: "primary.contrastText" }} />
                </IconButton>
                <IconButton
                    onClick={(e) => {
                        e.stopPropagation();
                        handleDelete();
                    }}
                    title={isOwner ? "Delete project" : "Remove from list"}
                >
                    <DeleteIcon sx={{ m: 0.7, color: "primary.contrastText" }} />
                </IconButton>
            </Box>
        </Paper>
    );
}
