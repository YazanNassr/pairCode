import { useState } from "react";
import ProjectList from "../components/ProjectList.tsx";
import CreateProjectDialog from "../components/mui/CreateProjectDialog.tsx";
import MainResponsiveAppBar from "../components/mui/MainResponsiveAppBar.tsx";
import RequireAuth from "../components/RequireAuth.tsx";
import { Container, Divider } from "@mui/material";
import Box from "@mui/material/Box";
import Typography from "@mui/material/Typography";
import Button from "@mui/material/Button";

export default function HomePage() {
    const [open, setOpen] = useState(false);

    return (
        <RequireAuth>
            <MainResponsiveAppBar />
            <Container sx={{ display: "flex", flexDirection: "column" }}>
                <Box
                    sx={{
                        m: 2,
                        display: "flex",
                        justifyContent: "space-between",
                        alignItems: "flex-end",
                        flexWrap: "wrap",
                    }}
                >
                    <Typography variant={"h5"}>Your Projects</Typography>
                    <Button onClick={() => setOpen(true)} variant="contained">
                        Add a Project
                    </Button>
                    {open && (
                        <CreateProjectDialog open={open} handleClose={() => setOpen(false)} />
                    )}
                </Box>
                <Divider orientation="horizontal" variant="middle" />
                <ProjectList />
            </Container>
        </RequireAuth>
    );
}
