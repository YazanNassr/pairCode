import Button from '@mui/material/Button';
import TextField from '@mui/material/TextField';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import { useState } from "react";

type FormDialogProps = {
    open: boolean;
    handleClose: () => void;
    chosenDirPath: string;
    onCreateFile: (filePath: string) => void;
};

export default function FormDialog({
    open,
    handleClose,
    chosenDirPath,
    onCreateFile,
}: FormDialogProps) {
    const [newFilePath, setNewFilePath] = useState<string>(chosenDirPath + '/');

    return (
        <Dialog
            open={open}
            onClose={handleClose}
            PaperProps={{
                component: 'form',
                onSubmit: (event: React.FormEvent<HTMLFormElement>) => {
                    event.preventDefault();
                    onCreateFile(newFilePath);
                    handleClose();
                },
            }}
        >
            <DialogTitle>Add New File</DialogTitle>
            <DialogContent>
                <TextField
                    autoFocus
                    required
                    margin="dense"
                    label="File Path"
                    type="text"
                    fullWidth
                    value={newFilePath}
                    onChange={(e) => setNewFilePath(e.target.value)}
                />
            </DialogContent>
            <DialogActions>
                <Button onClick={handleClose}>Cancel</Button>
                <Button type="submit">Add</Button>
            </DialogActions>
        </Dialog>
    );
}
