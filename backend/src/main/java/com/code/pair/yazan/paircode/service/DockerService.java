package com.code.pair.yazan.paircode.service;

import com.code.pair.yazan.paircode.domain.ProjectFile;

import java.util.List;

/**
 * Runs user code in isolated Docker containers.
 */
public interface DockerService {

    /**
     * @param files     ephemeral copy of project files including stdin input file
     * @param inputFile path to the stdin file inside the container
     * @param mainFile  entry file path inside the container
     * @return captured stdout/stderr from execution
     */
    String runPythonCode(List<ProjectFile> files, String inputFile, String mainFile);

    /**
     * @param files     ephemeral copy of project files including stdin input file
     * @param inputFile path to the stdin file inside the container
     * @param mainFile  entry file path inside the container
     * @return captured stdout/stderr from execution
     */
    String runJSCode(List<ProjectFile> files, String inputFile, String mainFile);
}
