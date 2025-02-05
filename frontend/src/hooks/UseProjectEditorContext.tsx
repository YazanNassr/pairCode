import { createContext, useContext } from "react";
import { ProjectEditorStateContext } from "../types/types.ts";

export const UseProjectEditorContext =
    createContext<ProjectEditorStateContext | undefined>(undefined);

/** Accesses project editor state from context. */
export function useProjectEditorContext(): ProjectEditorStateContext {
    const context = useContext(UseProjectEditorContext);

    if (context == undefined) {
        throw new Error("useProjectEditorContext must be used within UseProjectEditorContext");
    }

    return context;
}
