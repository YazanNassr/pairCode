package com.code.pair.yazan.paircode.dsa;

import com.code.pair.yazan.paircode.domain.Project;
import com.code.pair.yazan.paircode.exception.InvalidRequestException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Live editor session state for a Project, backed by in-memory file buffers.
 */
public class InMemoryProject {
    private final Project activeProject;
    private final Map<String, InMemoryFile> inMemFiles = new ConcurrentHashMap<>();

    public InMemoryProject(Project activeProject) {
        this.activeProject = activeProject;
        for (var file : activeProject.getFiles()) {
            inMemFiles.put(file.getFilePath(), new InMemoryFile(file.getSourceCode()));
        }
    }

    /**
     * @param modification collaborative edits for a single file
     * @return applied modifications
     * @throws InvalidRequestException when modifications or target file are invalid
     */
    public List<TextModification> applyModification(List<TextModification> modification) {
        if (modification == null || modification.isEmpty()) {
            throw new InvalidRequestException("At least one modification is required");
        }

        InMemoryFile file = inMemFiles.get(modification.getFirst().getFilePath());

        if (file == null) {
            throw new InvalidRequestException(
                    "File not found in editor session: " + modification.getFirst().getFilePath());
        }

        file.addModification(modification);
        return file.applyModifications();
    }

    /**
     * @param filePath path of the file within the Project
     * @return read payload with full file contents
     * @throws InvalidRequestException when the file is not in the session
     */
    public TextModification getFileContent(String filePath) {
        InMemoryFile file = inMemFiles.get(filePath);
        if (file == null) {
            throw new InvalidRequestException("File not found in editor session: " + filePath);
        }
        return TextModification.builder()
                .start(0).end(0)
                .newVal(file.getText())
                .fileVersion(file.getVersion())
                .build();
    }

    /**
     * @return a Project snapshot with current in-memory file contents
     */
    public Project toProject() {
        var tmp = inMemFiles;

        activeProject.getFiles().forEach(
                file -> file.setSourceCode(
                        tmp.get(file.getFilePath()).getText()));

        return activeProject;
    }
}
