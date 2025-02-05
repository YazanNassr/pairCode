package com.code.pair.yazan.paircode.exception;

/**
 * Thrown when a {@linkplain com.code.pair.yazan.paircode.domain.Project Project} id does not exist.
 */
public class ProjectNotFoundException extends RuntimeException {

    public ProjectNotFoundException(String projectId) {
        super("Project not found: " + projectId);
    }
}
