package com.code.pair.yazan.paircode.service;

/**
 * Runs Project code in Docker without mutating persisted state.
 */
public interface RunService {

    /**
     * Executes the main File with stdin input in an isolated container.
     *
     * @param projectId    the Project id
     * @param userId       the authenticated User
     * @param mainFilePath path to the entry File
     * @param input        stdin content
     * @param language     execution language (python, javascript)
     * @return program output
     */
    String run(String projectId, String userId, String mainFilePath, String input, String language);
}
