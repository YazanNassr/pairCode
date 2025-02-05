package com.code.pair.yazan.paircode.codeExecution.configClasses.directive;


import com.code.pair.yazan.paircode.codeExecution.configClasses.*;

import java.util.List;

public class ContainerConfigsPythonCode implements ContainerConfigs {
    private final String timeToSleep = "10";

    public ContainerCreationConfig.ContainerCreationConfigBuilder creationConfig() {
        return ContainerCreationConfig.builder()
                .imageName("python")
                .imageTag("3")
                .autoRemove(true)
                .workingDir("/usr/src/app/")
                .command(List.of("sleep", timeToSleep));
    }

    @Override
    public ContainerStartupConfig.ContainerStartupConfigBuilder startupConfig(String containerId) {
        return ContainerStartupConfig.builder()
                .containerId(containerId);
    }

    @Override
    public ContainerExecCreationConfig.ContainerExecCreationConfigBuilder execCreationConfigRunCode(
            String containerId, String mainFile, String inputFile) {

        return execCreationConfig(containerId,
                List.of("bash", "-c",
                        String.format("python '%s' < '%s'", mainFile, inputFile))
        );
    }

    @Override
    public ContainerExecStartupConfig.ContainerExecStartupConfigBuilder execStartupConfig(String execId) {
        return ContainerExecStartupConfig.builder()
                .execId(execId);
    }

    @Override
    public ContainerKillingConfig.ContainerKillingConfigBuilder killingConfig(String containerId) {
        return ContainerKillingConfig.builder()
                .containerId(containerId)
                .signal(ContainerKillingConfig.SIGNAL.SIGKILL);
    }

    @Override
    public ContainerRemovalConfig.ContainerRemovalConfigBuilder removalConfig(String containerId) {
        return ContainerRemovalConfig.builder()
                .containerId(containerId)
                .deleteVolumes(true)
                .force(true);
    }

    private ContainerExecCreationConfig.ContainerExecCreationConfigBuilder execCreationConfig(
            String containerId, List<String> command) {
        return ContainerExecCreationConfig.builder()
                .containerId(containerId)
                .attachStdout(true)
                .attachStderr(true)
                .command(command);
    }
}
