package com.code.pair.yazan.paircode.codeExecution.client.impl;

import com.code.pair.yazan.paircode.codeExecution.client.DockerClient;
import com.code.pair.yazan.paircode.codeExecution.configClasses.*;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@AllArgsConstructor
public class DockerClientImpl implements DockerClient {
    private RestClient restClient;

    @Override
    public String createContainer(ContainerCreationConfig config) {
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restClient.post()
                .uri("/containers/create")
                .body(Map.of(
                        "Image", config.imageName() + ":" + config.imageTag(),
                        "WorkingDir", config.workingDir(),
                        "Cmd", config.command(),
                        "HostConfig", Map.of("AutoRemove", config.autoRemove())
                ))
                .retrieve()
                .body(Map.class);
        return (String) response.get("Id");
    }

    @Override
    public void startContainer(ContainerStartupConfig config) {
        restClient.post()
                .uri("/containers/{containerId}/start", config.containerId())
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public String createExec(ContainerExecCreationConfig config) {
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restClient.post()
                .uri("/containers/{containerId}/exec", config.containerId())
                .body(Map.of(
                        "Cmd", config.command(),
                        "AttachStdout", config.attachStdout(),
                        "AttachStderr", config.attachStderr()))
                .retrieve()
                .body(Map.class);
        return (String) response.get("Id");
    }

    @Override
    public String startExec(ContainerExecStartupConfig config) {
        byte[] rawOutput = restClient.post()
                .uri("/exec/{execId}/start", config.execId())
                .body(Map.of("Detach", false, "Tty", false))
                .retrieve()
                .body(byte[].class);
        return parseExecOutput(rawOutput);
    }

    private static String parseExecOutput(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }

        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();
        int offset = 0;

        while (offset + 8 <= data.length) {
            int streamType = data[offset] & 0xFF;
            int size = ((data[offset + 4] & 0xFF) << 24)
                    | ((data[offset + 5] & 0xFF) << 16)
                    | ((data[offset + 6] & 0xFF) << 8)
                    | (data[offset + 7] & 0xFF);
            offset += 8;
            if (size < 0 || offset + size > data.length) {
                break;
            }
            String chunk = new String(data, offset, size, StandardCharsets.UTF_8);
            offset += size;
            if (streamType == 2) {
                stderr.append(chunk);
            } else {
                stdout.append(chunk);
            }
        }

        if (stdout.length() > 0) {
            return filterPrintableAscii(stdout.toString());
        }
        if (stderr.length() > 0) {
            return filterPrintableAscii(stderr.toString());
        }
        return filterPrintableAscii(new String(data, StandardCharsets.UTF_8));
    }

    private static String filterPrintableAscii(String value) {
        return value.codePoints()
                .filter(c -> c == '\t' || c == '\n' || c > 31 && c < 128)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    @Override
    public void putArchive(String containerId, String path, byte[] tarBytes) {
        restClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/containers/{containerId}/archive")
                        .queryParam("path", path)
                        .build(containerId))
                .contentType(MediaType.parseMediaType("application/x-tar"))
                .body(tarBytes)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void removeContainer(ContainerRemovalConfig config) {
        restClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/containers/{containerId}")
                        .queryParam("v", config.deleteVolumes())
                        .queryParam("force", config.force())
                        .build(config.containerId()))
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void killContainer(ContainerKillingConfig config) {
        restClient.post()
                .uri("/containers/{containerId}/kill", config.containerId())
                .body(Map.of("signal", config.signal()))
                .retrieve()
                .toBodilessEntity();
    }
}
