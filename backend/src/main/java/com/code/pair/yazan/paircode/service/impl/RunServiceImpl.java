package com.code.pair.yazan.paircode.service.impl;

import com.code.pair.yazan.paircode.domain.ProjectFile;
import com.code.pair.yazan.paircode.domain.Project;
import com.code.pair.yazan.paircode.codeExecution.ExecutionPathValidator;
import com.code.pair.yazan.paircode.exception.InvalidRequestException;
import com.code.pair.yazan.paircode.service.CollaborationService;
import com.code.pair.yazan.paircode.service.DockerService;
import com.code.pair.yazan.paircode.service.RunService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Default {@link RunService} using ephemeral file copies for execution.
 */
@Service
@AllArgsConstructor
public class RunServiceImpl implements RunService {

    private final CollaborationService collaborationService;
    private final DockerService dockerService;

    @Override
    public String run(String projectId, String userId, String mainFilePath, String input, String language) {
        ExecutionPathValidator.validateRelativePath(mainFilePath);

        Project project = collaborationService.getExecutionSnapshot(projectId, userId);
        List<ProjectFile> executionFiles = copyFilesWithInput(project.getFiles(), input);

        return switch (language.toLowerCase()) {
            case "python" -> dockerService.runPythonCode(executionFiles, "./input.txt", mainFilePath);
            case "javascript" -> dockerService.runJSCode(executionFiles, "./input.txt", mainFilePath);
            default -> throw new InvalidRequestException("Invalid language: " + language);
        };
    }

    private List<ProjectFile> copyFilesWithInput(List<ProjectFile> sourceFiles, String input) {
        List<ProjectFile> copies = new ArrayList<>();
        for (ProjectFile file : sourceFiles) {
            copies.add(ProjectFile.builder()
                    .parentPath(file.getParentPath())
                    .fileName(file.getFileName())
                    .sourceCode(file.getSourceCode())
                    .build());
        }
        copies.add(ProjectFile.builder()
                .parentPath(".")
                .fileName("input.txt")
                .sourceCode(input)
                .build());
        return copies;
    }
}
