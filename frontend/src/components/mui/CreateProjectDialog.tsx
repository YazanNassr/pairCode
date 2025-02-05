import * as React from "react";
import Button from "@mui/material/Button";
import TextField from "@mui/material/TextField";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogContentText from "@mui/material/DialogContentText";
import DialogTitle from "@mui/material/DialogTitle";
import { useState } from "react";
import { createProject } from "../../services/api/projectApi.ts";
import { File } from "../../types/types.ts";
import { useQueryClient } from "@tanstack/react-query";
import { toast } from "react-toastify";

const defaultFiles: File[] = [
    { parentPath: ".", fileName: "main", filePath: "./main", sourceCode: "" },
    { parentPath: "utils", fileName: "util.py", filePath: "utils/util.py", sourceCode: "" },
    { parentPath: "utils", fileName: "util.js", filePath: "utils/util.js", sourceCode: "" },
    { parentPath: "lib", fileName: "lib.py", filePath: "lib/lib.py", sourceCode: "" },
    { parentPath: "lib", fileName: "lib.js", filePath: "lib/lib.js", sourceCode: "" },
];

type CreateProjectDialogProps = {
    open: boolean;
    handleClose: () => void;
};

export default function CreateProjectDialog({ open, handleClose }: CreateProjectDialogProps) {
    const [projectName, setProjectName] = useState<string>("");
    const queryClient = useQueryClient();

    return (
        <Dialog
            open={open}
            onClose={handleClose}
            PaperProps={{
                component: "form",
                onSubmit: (event: React.FormEvent<HTMLFormElement>) => {
                    event.preventDefault();
                    createProject({
                        name: projectName || "untitled",
                        files: defaultFiles,
                    })
                        .then(() => {
                            queryClient.invalidateQueries({ queryKey: ["projects"] });
                            handleClose();
                        })
                        .catch(() => {
                            toast.error("Could not create project. Try logging in again.");
                        });
                },
            }}
        >
            <DialogTitle>New Project</DialogTitle>
            <DialogContent>
                <DialogContentText>Enter your new project name</DialogContentText>
                <TextField
                    autoFocus
                    required
                    margin="dense"
                    id="name"
                    name="name"
                    label="Project Name"
                    type="text"
                    fullWidth
                    variant="standard"
                    value={projectName}
                    onChange={(e) => setProjectName(e.target.value)}
                />
            </DialogContent>
            <DialogActions>
                <Button onClick={handleClose}>Cancel</Button>
                <Button type="submit">Create</Button>
            </DialogActions>
        </Dialog>
    );
}
