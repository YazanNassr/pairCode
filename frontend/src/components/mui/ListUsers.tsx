import Box from "@mui/material/Box";
import {List, ListItem, ListItemText} from "@mui/material";
import {IMessage, useStompClient, useSubscription} from "react-stomp-hooks";
import {useEffect} from "react";
import {useProjectEditorContext} from "../../hooks/UseProjectEditorContext.tsx";
import Typography from "@mui/material/Typography";
import {nameToColor} from "../NameToColor.ts"

function getContrastTextColor(hexColor: string) {
    const r = parseInt(hexColor.substring(0, 2), 16);
    const g = parseInt(hexColor.substring(2, 4), 16);
    const b = parseInt(hexColor.substring(4, 6), 16);
    const luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b;
    return luminance > 128 ? '#000000' : '#FFFFFF'; // Black for light backgrounds, white for dark backgrounds
}

export default function ListUsers() {
    const stompClient = useStompClient();

    const {editorState, setEditorState} = useProjectEditorContext()

    const encodedFilePath = encodeURIComponent(editorState.activeFile.filePath);
    const encodedProjectId = encodeURIComponent(editorState.project.id);
    const encodedUsername = encodeURIComponent(sessionStorage.getItem("username") ?? "");

    useSubscription(`/topic/users/${encodedProjectId}/${encodedFilePath}/${encodedUsername}`, (message: IMessage) => {
        setEditorState((prev) => ({ ...prev, activeUsers: JSON.parse(message.body) }));
    });


    useEffect(() => {
        if (stompClient) {
            stompClient.publish({
                destination: `/app/users/${encodedProjectId}/${encodedFilePath}/${encodedUsername}`,
            });
        }
    }, [stompClient, encodedProjectId, encodedFilePath, encodedUsername]);

    useEffect(() => {
        const style = document.createElement('style');
        style.textContent = '';
        editorState.activeUsers.forEach((user) => {
            const bcol = nameToColor(user)
            const tcol = getContrastTextColor(bcol);
            style.textContent += `
            .${user}-marker {
                color: ${tcol};
                background-color: #${bcol};
                transition: background-color 0.5s ease-out;
            }`
        })
        document.head.appendChild(style);
        return () => { document.head.removeChild(style) }
    }, [editorState.activeUsers]);

    return (
        <Box sx={{width: '100%', maxWidth: 360, bgcolor: 'background.paper'}}>
            <Typography
                variant="h6"
                component="div"
                sx={{
                    bgcolor: "primary.main",
                    fontSize: 16,
                    color: "white",
                    textTransform: "capitalize",
                    textAlign: "center",
                    padding: 1
                }}
            >
                Active Users
            </Typography>
            <List
                sx={{
                    width: '100%',
                    maxWidth: 360,
                    bgcolor: 'background.paper',
                }}
            >
                {
                    editorState.activeUsers.map((u, index) => {
                            const col = nameToColor(u);
                            return <ListItem
                                key={index}
                                sx={{
                                    '&:hover': {backgroundColor: 'rgba(0, 0, 0, 0.1)'},
                                    padding: '8px 16px',
                                }}
                            >
                                <ListItemText
                                    primary={u}
                                    sx={{
                                        color: `#${col}`,
                                        fontWeight: 500,
                                    }}
                                />
                            </ListItem>
                        }
                    )
                }
            </List>
        </Box>
    )
}