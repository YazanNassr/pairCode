import AppBar from '@mui/material/AppBar';
import Box from '@mui/material/Box';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import IconButton from '@mui/material/IconButton';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import SaveIcon from '@mui/icons-material/Save';
import CircularProgress from '@mui/material/CircularProgress';
import LanguageMenu from "./LanguageMenu.tsx";
import {useProjectEditorContext} from "../../hooks/UseProjectEditorContext.tsx";
import {runProject} from "../../services/api/projectApi.ts";
import {useStompClient, useSubscription} from "react-stomp-hooks";
import {useState} from 'react';
import {toast} from "react-toastify";

export default function RunResponsiveAppBar() {
    const {editorState, setEditorState} = useProjectEditorContext()
    const stompClient = useStompClient();
    const [isLoading, setIsLoading] = useState(false);
    const [isSaving, setIsSaving] = useState(false);

    const run = () => {
        setIsLoading(true);
        const projectId = editorState.project.id ?? ""
        const input = editorState.input
        const mainFilePath = editorState.activeFile.filePath
        const language = editorState.language.toLowerCase()

        runProject(projectId, input, mainFilePath, language)
            .then(out => setEditorState({...editorState, output: out}))
            .catch(() => toast.error("Error running project"))
            .finally(() => setIsLoading(false));
    }


    const encodedProjectId = encodeURIComponent(editorState.project.id);
    const username = sessionStorage.getItem("username") || "";
    const encodedUsername = encodeURIComponent(username);
    useSubscription(`/topic/save/${encodedProjectId}/${encodedUsername}`, () => setIsSaving(false));

    const save = () => {
        const projectId = editorState.project.id
        if (stompClient) {
            setIsSaving(true);
            stompClient.publish({
                destination: `/app/save/${projectId}`
            })
        } else {
            toast.error("Disconnected from server");
        }
    }

    return (
        <Box sx={{ flexGrow: 1 }}>
            <AppBar
                position="static"
            >
                <Toolbar
                    sx={{ bgcolor: "primary.dark" }}
                    variant="dense"
                >
                    <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
                        {editorState.project.name} : {editorState.activeFile.parentPath}/{editorState.activeFile.fileName}
                    </Typography>

                    <LanguageMenu />
                    <IconButton
                        size="small"
                        edge="end"
                        aria-label="run"
                        sx={{ mx: 2, my: 0.5 }}
                        onClick={save}
                        disabled={isSaving}
                    >
                        { isSaving ? (
                                <CircularProgress
                                    size={24}
                                    sx={{color: "primary.contrastText"}}
                                />
                            ) : (
                                <SaveIcon
                                    sx={{color: "primary.contrastText"}}
                                />
                            )
                        }
                    </IconButton>

                    <IconButton
                        size="small"
                        edge="end"
                        aria-label="run"
                        sx={{ mx: 2, my: 0.5 }}
                        onClick={run}
                        disabled={isLoading}
                    >
                        {isLoading ? (
                            <CircularProgress
                                size={24}
                                sx={{color: "primary.contrastText"}}
                            />
                        ) : (
                            <PlayArrowIcon
                                sx={{color: "primary.contrastText"}}
                            />
                        )}
                    </IconButton>
                </Toolbar>
            </AppBar>
        </Box>
    );
}
