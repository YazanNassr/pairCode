import React, { useState } from "react";
import Box from "@mui/material/Box";
import { SimpleTreeView } from "@mui/x-tree-view/SimpleTreeView";
import { TreeItem } from "@mui/x-tree-view/TreeItem";
import Typography from "@mui/material/Typography";
import { useProjectEditorContext } from "../../hooks/UseProjectEditorContext.tsx";
import FormDialog from "./CreateFileDialog.tsx";
import IconButton from "@mui/material/IconButton";
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import { toast } from "react-toastify";
import { File } from "../../types/types.ts";

type TreeNode = {
  id: string;
  name: string;
  fullPath: string;
  type: "file" | "directory";
  children?: TreeNode[];
};

const normalizePath = (path: string): string => {
  return path.replace(/^\.\//, "").replace(/\/\.\//g, "/");
};

const buildTree = (files: { filePath: string }[]): TreeNode => {
  const root: TreeNode = {
    id: "root",
    name: "root",
    type: "directory",
    fullPath: "",
    children: [],
  };

  const pathMap: Record<string, TreeNode> = { "": root };

  files.forEach(({ filePath }) => {
    const normalizedPath = normalizePath(filePath);
    if (normalizedPath === "." || normalizedPath === "..") return;
    const segments = normalizedPath.split("/").filter(Boolean);

    segments.reduce((parent, segment, idx) => {
      const isFile = idx === segments.length - 1;
      const fullPath = segments.slice(0, idx + 1).join("/");
      const id = fullPath;

      if (!pathMap[id]) {
        const node: TreeNode = {
          id,
          name: segment,
          type: isFile ? "file" : "directory",
          fullPath,
          children: isFile ? undefined : [],
        };

        parent.children?.push(node);
        if (!isFile) pathMap[id] = node;
      }

      return pathMap[id];
    }, root);
  });

  return root;
};

function toProjectFile(filePath: string): File {
  const normalized = normalizePath(filePath);
  const lastSlash = normalized.lastIndexOf("/");
  const parentPath = lastSlash >= 0 ? normalized.substring(0, lastSlash) || "." : ".";
  const fileName = lastSlash >= 0 ? normalized.substring(lastSlash + 1) : normalized;

  return {
    parentPath,
    fileName,
    filePath: `./${normalized}`,
    sourceCode: "",
  };
}

export default function FileTree() {
  const { editorState, setEditorState } = useProjectEditorContext();
  const fileTree = buildTree(editorState.project.files);

  const [open, setOpen] = useState(false);
  const [chosenDirPath, setChosenDirPath] = useState("");

  const handleDelete = (event: React.MouseEvent, fullPath: string) => {
    event.stopPropagation();
    toast.info(`Removing "${fullPath}" is not implemented yet.`);
  };

  const handleChoose = (option: string) => {
    setEditorState(state => ({
      ...state,
      activeFile: state.project.files.find(file => file.filePath === option || file.filePath === "./"+option) || state.activeFile
    }));
  };

  const handleCreateFile = (filePath: string) => {
    const newFile = toProjectFile(filePath);
    setEditorState(state => ({
      ...state,
      project: {
        ...state.project,
        files: [...state.project.files, newFile],
      },
      activeFile: newFile,
    }));
    setOpen(false);
  };

  const renderTreeItems = (node: TreeNode): React.ReactNode => {
    if (node.type === "directory") {
      return (
          <TreeItem key={node.id} itemId={node.id} label={
            <Box sx={{ display: "flex", alignItems: "center", gap: 1, "&:hover .action-icon": { opacity: 1, visibility: "visible" } }}>
              {node.name}
              <Box sx={{ flexGrow: 1 }} />
              <IconButton
                  size="small"
                  className="action-icon"
                  sx={{
                    opacity: 0,
                    visibility: "hidden",
                    transition: "opacity 0.2s, visibility 0.2s",
                  }}
                  onClick={(event) => {
                    event.stopPropagation();
                    setOpen(true);
                    setChosenDirPath(node.fullPath)
                  }}
              >
                <AddIcon fontSize="small" />
              </IconButton>
              <IconButton
                  size="small"
                  className="action-icon"
                  sx={{
                    opacity: 0,
                    visibility: "hidden",
                    transition: "opacity 0.2s, visibility 0.2s",
                  }}
                  onClick={(event) => handleDelete(event, node.fullPath)}
              >
                <DeleteIcon fontSize="small" />
              </IconButton>
            </Box>
          }
          >
            {node.children?.map((child) => renderTreeItems(child))}
          </TreeItem>
      );
    }
    return <TreeItem key={node.id} itemId={node.id} label={
      <Box sx={{ display: "flex", alignItems: "center", gap: 1, "&:hover .action-icon": { opacity: 1, visibility: "visible" } }}>
        {node.name}
        <Box sx={{ flexGrow: 1 }} />
        <IconButton
            size="small"
            className="action-icon"
            sx={{
              opacity: 0,
              visibility: "hidden",
              transition: "opacity 0.2s, visibility 0.2s",
            }}
            onClick={(event) => handleDelete(event, node.fullPath)}
        >
          <DeleteIcon fontSize="small" />
        </IconButton>
      </Box>
    } onClick={() => handleChoose(node.fullPath)} />;
  };

  return (
      <Box sx={{ minHeight: 352, minWidth: 250 }}>
        <SimpleTreeView>
          <Typography
              variant="h6"
              component="div"
              sx={{
                bgcolor: "primary.main",
                fontSize: 16,
                color: "white",
                textTransform: "capitalize",
                textAlign: "left",
                padding: 1,
              }}
          >
            Files
          </Typography>
          {fileTree.children?.map((node) => renderTreeItems(node))}
        </SimpleTreeView>

        {open && (
            <FormDialog
                open={open}
                handleClose={() => setOpen(false)}
                chosenDirPath={chosenDirPath}
                onCreateFile={handleCreateFile}
            />
        )}

      </Box>
  );
}
