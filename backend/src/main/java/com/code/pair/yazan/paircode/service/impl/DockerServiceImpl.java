package com.code.pair.yazan.paircode.service.impl;

import com.code.pair.yazan.paircode.codeExecution.DockerArchiveBuilder;
import com.code.pair.yazan.paircode.codeExecution.client.DockerClient;
import com.code.pair.yazan.paircode.codeExecution.configClasses.ContainerCreationConfig;
import com.code.pair.yazan.paircode.codeExecution.configClasses.directive.ContainerConfigs;
import com.code.pair.yazan.paircode.codeExecution.configClasses.directive.ContainerConfigsJavaScriptCode;
import com.code.pair.yazan.paircode.codeExecution.configClasses.directive.ContainerConfigsPythonCode;
import com.code.pair.yazan.paircode.domain.ProjectFile;
import com.code.pair.yazan.paircode.service.DockerService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Orchestrates Docker container lifecycle for code execution.
 */
@Service
@AllArgsConstructor
public class DockerServiceImpl implements DockerService {
    private DockerClient dockerClient;

    @Override
    public String runPythonCode(List<ProjectFile> files, String inputFile, String mainFile) {
        return runCode(files, inputFile, mainFile, new ContainerConfigsPythonCode());
    }

    @Override
    public String runJSCode(List<ProjectFile> files, String inputFile, String mainFile) {
        return runCode(files, inputFile, mainFile, new ContainerConfigsJavaScriptCode());
    }

    private String runCode(List<ProjectFile> files, String inputFile, String mainFile, ContainerConfigs configs) {
        ContainerCreationConfig creationConfig = configs.creationConfig().build();
        String containerId = dockerClient.createContainer(creationConfig);
        dockerClient.startContainer(configs.startupConfig(containerId).build());

        byte[] archive = DockerArchiveBuilder.buildTar(files);
        dockerClient.putArchive(containerId, creationConfig.workingDir(), archive);

        String execId = dockerClient.createExec(
                configs.execCreationConfigRunCode(containerId, mainFile, inputFile).build());
        String res = dockerClient.startExec(configs.execStartupConfig(execId).build());
        dockerClient.killContainer(configs.killingConfig(containerId).build());

        return res;
    }
}
