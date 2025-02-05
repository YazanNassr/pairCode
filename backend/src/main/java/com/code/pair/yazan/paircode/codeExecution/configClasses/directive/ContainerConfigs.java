package com.code.pair.yazan.paircode.codeExecution.configClasses.directive;

import com.code.pair.yazan.paircode.codeExecution.configClasses.*;

public interface ContainerConfigs {
    ContainerCreationConfig.ContainerCreationConfigBuilder
        creationConfig();
    ContainerStartupConfig.ContainerStartupConfigBuilder
        startupConfig(String containerId);

    ContainerExecCreationConfig.ContainerExecCreationConfigBuilder
        execCreationConfigRunCode(String containerId, String mainFile, String inputFile);

    ContainerExecStartupConfig.ContainerExecStartupConfigBuilder
        execStartupConfig(String execId);

    ContainerKillingConfig.ContainerKillingConfigBuilder
        killingConfig(String containerId);
    ContainerRemovalConfig.ContainerRemovalConfigBuilder
        removalConfig(String containerId);
}
