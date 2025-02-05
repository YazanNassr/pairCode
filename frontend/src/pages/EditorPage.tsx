import CodeEditor from "../components/CodeEditor.tsx";
import Grid from "@mui/material/Grid2";
import RunAppBar from "../components/mui/RunResponsiveAppBar.tsx";
import TextArea from "../components/ui/TextArea.tsx";
import { ProjectEditorState } from "../types/types.ts";
import { UseProjectEditorContext } from "../hooks/UseProjectEditorContext.tsx";
import { useState } from "react";
import MainResponsiveAppBar from "../components/mui/MainResponsiveAppBar.tsx";
import FileTree from "../components/mui/FileTree.tsx";
import ListUsers from "../components/mui/ListUsers.tsx";

type EditorPageProps = {
    initialState: ProjectEditorState;
};

export default function EditorPage({ initialState }: EditorPageProps) {
    const [editorState, setEditorState] = useState<ProjectEditorState>(initialState);
    const setInputText = (newVal: string) =>
        setEditorState((prev) => ({ ...prev, input: newVal }));
    const setOutputText = (newVal: string) =>
        setEditorState((prev) => ({ ...prev, output: newVal }));

    return (
        <UseProjectEditorContext.Provider value={{ editorState, setEditorState }}>
            <MainResponsiveAppBar />
            <Grid container columns={12}>
                <Grid size={12}>
                    <RunAppBar />
                </Grid>

                <Grid size={{ xs: 2, md: 2 }}>
                    <FileTree />
                    <ListUsers />
                </Grid>

                <Grid size={{ xs: 10, md: 7 }}>
                    <CodeEditor />
                </Grid>

                <Grid size={{ xs: 12, md: 3 }} height={{ xs: 50, md: 20 }}>
                    <Grid size={{ xs: 12, md: 12 }}>
                        <TextArea
                            text={editorState.input}
                            setText={setInputText}
                            title={"Input"}
                            readonly={false}
                        />
                    </Grid>

                    <Grid size={{ xs: 12, md: 12 }}>
                        <TextArea
                            text={editorState.output}
                            setText={setOutputText}
                            title={"Output"}
                            readonly={true}
                        />
                    </Grid>
                </Grid>
            </Grid>
        </UseProjectEditorContext.Provider>
    );
}
