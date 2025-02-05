package com.code.pair.yazan.paircode.controller.stomp;

import com.code.pair.yazan.paircode.dsa.TextModification;
import com.code.pair.yazan.paircode.service.CollaborationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;

/**
 * STOMP handlers for live editor session collaboration.
 */
@Slf4j
@Controller
@AllArgsConstructor
public class CollaborationController {

    private final CollaborationService collaborationService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/users/{projectId}/{filepath}/{username}")
    public void inquireActiveUsers(@DestinationVariable String projectId,
                                   @DestinationVariable String filepath,
                                   @DestinationVariable String username,
                                   Principal principal) {
        String decodedProjectId = decode(projectId);
        collaborationService.loadSession(decodedProjectId, principal.getName());
        String path = String.format("/topic/users/%s/%s/%s", projectId, filepath, username);
        String topic = String.format("/topic/modify/%s/%s", projectId, filepath);
        messagingTemplate.convertAndSend(path, collaborationService.getActiveUsers(topic));
    }

    @MessageMapping("/read/{projectId}/{filepath}/{username}")
    public void readFile(@DestinationVariable String projectId,
                         @DestinationVariable String filepath,
                         @DestinationVariable String username,
                         Principal principal) {
        String decodedProjectId = decode(projectId);
        String decodedFilePath = decode(filepath);
        var result = collaborationService.readFile(decodedProjectId, decodedFilePath, principal.getName());
        String path = String.format("/topic/read/%s/%s/%s", projectId, filepath, username);
        messagingTemplate.convertAndSend(path, result);
    }

    @MessageMapping("/modify/{projectId}/{filepath}")
    public void modify(@Payload List<TextModification> modification,
                       @DestinationVariable String projectId,
                       @DestinationVariable String filepath,
                       Principal principal) {
        String decodedProjectId = decode(projectId);
        String decodedFilePath = decode(filepath);
        for (TextModification mod : modification) {
            mod.setModifier(principal.getName());
            mod.setProjectId(decodedProjectId);
            mod.setFilePath(decodedFilePath);
        }
        String path = String.format("/topic/modify/%s/%s", projectId, filepath);
        var applied = collaborationService.applyModifications(
                decodedProjectId, decodedFilePath, modification, principal.getName());
        messagingTemplate.convertAndSend(path, applied);
    }

    @MessageMapping("/save/{projectId}")
    public void save(@DestinationVariable String projectId, Principal principal) {
        String decodedProjectId = decode(projectId);
        collaborationService.saveSession(decodedProjectId, principal.getName());
        String encodedUsername = URLEncoder.encode(principal.getName(), StandardCharsets.UTF_8);
        String path = String.format("/topic/save/%s/%s", projectId, encodedUsername);
        messagingTemplate.convertAndSend(path, true);
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
