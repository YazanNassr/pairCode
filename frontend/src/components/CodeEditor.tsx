import Editor, { OnMount } from "@monaco-editor/react";
import type { editor } from "monaco-editor";
import { useProjectEditorContext } from "../hooks/UseProjectEditorContext.tsx";
import { TextModification } from "../types/types.ts";
import { IMessage, useStompClient, useSubscription } from "react-stomp-hooks";
import { useEffect, useRef, useState } from "react";
import "./styles.css";

export default function CodeEditor() {
    const stompClient = useStompClient();
    const { editorState } = useProjectEditorContext();
    const editorRef = useRef<editor.IStandaloneCodeEditor | null>(null);
    const monacoRef = useRef<typeof import("monaco-editor") | null>(null);

    const lastSyncedVersionRef = useRef<number>(0);
    const [hasLoaded, setHasLoaded] = useState(false);
    const dontSendRef = useRef<boolean>(false);
    const highlightTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    const encodedFilePath = encodeURIComponent(editorState.activeFile.filePath);
    const encodedProjectId = encodeURIComponent(editorState.project.id);
    const username = sessionStorage.getItem("username") || "";
    const encodedUsername = encodeURIComponent(username);

    const handleEditorDidMount: OnMount = (editorInstance, monaco) => {
        setHasLoaded(true);
        editorRef.current = editorInstance;
        monacoRef.current = monaco;
    };

    function sendModifications(modifications: TextModification[]) {
        if (dontSendRef.current) {
            dontSendRef.current = false;
            return;
        }

        if (!stompClient) {
            return;
        }

        stompClient.publish({
            destination: `/app/modify/${encodedProjectId}/${encodedFilePath}`,
            body: JSON.stringify(modifications),
        });
    }

    const applyModification = (mod: TextModification) => {
        lastSyncedVersionRef.current = mod.fileVersion + 1;

        const editorInstance = editorRef.current;
        const monaco = monacoRef.current;
        if (!editorInstance || !monaco) return;

        const model = editorInstance.getModel();
        if (!model) return;

        if (mod.modifier !== username) {
            const editRange = new monaco.Range(
                model.getPositionAt(mod.start).lineNumber,
                model.getPositionAt(mod.start).column,
                model.getPositionAt(mod.end).lineNumber,
                model.getPositionAt(mod.end).column
            );

            dontSendRef.current = true;
            model.pushEditOperations(
                [],
                [{ range: editRange, text: mod.newVal, forceMoveMarkers: false }],
                () => null
            );
        }

        const newRange = new monaco.Range(
            model.getPositionAt(mod.start).lineNumber,
            model.getPositionAt(mod.start).column,
            model.getPositionAt(mod.end).lineNumber,
            model.getPositionAt(mod.end).column
        );

        const overlappingDecorations = model.getDecorationsInRange(newRange) || [];
        editorInstance.deltaDecorations(
            overlappingDecorations.map((d) => d.id),
            []
        );

        editorInstance.deltaDecorations(
            [],
            [
                {
                    range: newRange,
                    options: {
                        hoverMessage: { value: `Modified By: **${mod.modifier}**` },
                    },
                },
            ]
        );

        if (mod.modifier !== username) {
            const highlightRange = new monaco.Range(
                model.getPositionAt(mod.start).lineNumber,
                model.getPositionAt(mod.start).column,
                model.getPositionAt(mod.start + mod.newVal.length).lineNumber,
                model.getPositionAt(mod.start + mod.newVal.length).column
            );

            const highlightDecorationIds = editorInstance.deltaDecorations(
                [],
                [
                    {
                        range: highlightRange,
                        options: {
                            inlineClassName: `${mod.modifier}-marker`,
                        },
                    },
                ]
            );

            if (highlightTimeoutRef.current !== null) {
                clearTimeout(highlightTimeoutRef.current);
            }

            highlightTimeoutRef.current = setTimeout(() => {
                if (editorRef.current === editorInstance) {
                    editorInstance.deltaDecorations(highlightDecorationIds, []);
                }
                highlightTimeoutRef.current = null;
            }, 250);
        }
    };

    useSubscription(
        `/topic/read/${encodedProjectId}/${encodedFilePath}/${encodedUsername}`,
        (message: IMessage) => {
            const mod: TextModification = JSON.parse(message.body);
            if (mod.newVal === "") return;
            mod.modifier = "Initial File";
            applyModification(mod);
        }
    );

    useSubscription(
        `/topic/modify/${encodedProjectId}/${encodedFilePath}`,
        (message: IMessage) => {
            const mods: TextModification[] = JSON.parse(message.body);
            for (const mod of mods) {
                applyModification(mod);
            }
        }
    );

    function handleEditorChange(
        _value: string | undefined,
        event: editor.IModelContentChangedEvent
    ) {
        const modifications: TextModification[] = event.changes.map((c) => ({
            projectId: editorState.project.id,
            filePath: editorState.activeFile.filePath,
            start: c.rangeOffset,
            end: c.rangeOffset + c.rangeLength,
            newVal: c.text,
            fileVersion: lastSyncedVersionRef.current,
            modifier: username,
        }));

        sendModifications(modifications);
    }

    useEffect(() => {
        if (hasLoaded && stompClient && editorRef.current) {
            const editorInstance = editorRef.current;
            if (editorInstance.getValue() !== "") {
                dontSendRef.current = true;
                editorInstance.setValue("");
            }
            stompClient.publish({
                destination: `/app/read/${encodedProjectId}/${encodedFilePath}/${encodedUsername}`,
            });
        }
    }, [hasLoaded, stompClient, encodedProjectId, encodedFilePath, encodedUsername]);

    useEffect(() => {
        return () => {
            if (highlightTimeoutRef.current !== null) {
                clearTimeout(highlightTimeoutRef.current);
            }
        };
    }, []);

    return (
        <Editor
            height="82vh"
            defaultLanguage="python"
            language={editorState.language.toLowerCase()}
            theme="vs-dark"
            onChange={handleEditorChange}
            onMount={handleEditorDidMount}
            options={{
                hover: { enabled: true },
            }}
        />
    );
}
