import React from "react";

export type User = {
    username: string;
    password: string;
};

export type File = {
    parentPath: string;
    filePath: string;
    fileName: string;
    sourceCode: string;
};

export type Project = {
    id: string;
    name: string;
    ownerId: string;
    files: File[];
};

/** Ephemeral editor UI state (not persisted as a unit). */
export type ProjectEditorState = {
    project: Project;
    activeFile: File;
    activeUsers: string[];
    language: string;
    input: string;
    output: string;
};

export type ProjectEditorStateContext = {
    editorState: ProjectEditorState;
    setEditorState: React.Dispatch<React.SetStateAction<ProjectEditorState>>;
};

export type TextModification = {
    projectId: string;
    filePath: string;
    start: number;
    end: number;
    newVal: string;
    fileVersion: number;
    modifier: string;
};
