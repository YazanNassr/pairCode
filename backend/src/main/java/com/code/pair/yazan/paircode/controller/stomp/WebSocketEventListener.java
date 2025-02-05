package com.code.pair.yazan.paircode.controller.stomp;

import com.code.pair.yazan.paircode.service.CollaborationService;
import com.code.pair.yazan.paircode.service.PresenceTracker;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Tracks STOMP subscriptions for editor presence notifications and session lifecycle.
 */
@Component
public class WebSocketEventListener implements PresenceTracker {

    private static final String MODIFY_TOPIC_PREFIX = "/topic/modify/";

    private final ConcurrentHashMap<String, Set<String>> topicSubscriptions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> sessionToTopic = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> projectSubscriberCounts = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;
    private final CollaborationService collaborationService;

    public WebSocketEventListener(
            SimpMessagingTemplate messagingTemplate,
            @Lazy CollaborationService collaborationService) {
        this.messagingTemplate = messagingTemplate;
        this.collaborationService = collaborationService;
    }

    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String topic = headerAccessor.getDestination();
        String sessionId = headerAccessor.getSessionId();

        if (sessionId == null) return;
        if (topic == null) return;
        if (event.getUser() == null) return;

        String username = event.getUser().getName();

        sessionToTopic.put(sessionId, topic);
        topicSubscriptions.computeIfAbsent(topic, k -> new ConcurrentSkipListSet<>()).add(username);
        incrementProjectSubscriberCount(topic);
    }

    @EventListener
    public void handleSessionUnsubscribeEvent(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        if (sessionId == null || event.getUser() == null) {
            return;
        }
        String topic = sessionToTopic.get(sessionId);
        if (topic == null) {
            return;
        }
        String username = event.getUser().getName();
        removeSubscriber(topic, username);
        sessionToTopic.remove(sessionId);
    }

    @EventListener
    public void handleSessionDisconnectEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        if (sessionId == null || event.getUser() == null) {
            return;
        }
        String topic = sessionToTopic.get(sessionId);
        if (topic == null) {
            return;
        }
        String username = event.getUser().getName();
        removeSubscriber(topic, username);
        sessionToTopic.remove(sessionId);
    }

    @Override
    public Set<String> getSubscribers(String topic) {
        return topicSubscriptions.getOrDefault(topic, new HashSet<>());
    }

    private void removeSubscriber(String topic, String username) {
        Set<String> subscribers = topicSubscriptions.get(topic);
        if (subscribers != null) {
            subscribers.remove(username);
        }
        decrementProjectSubscriberCount(topic);
    }

    void incrementProjectSubscriberCount(String topic) {
        parseProjectIdFromModifyTopic(topic).ifPresent(projectId ->
                projectSubscriberCounts.merge(projectId, 1, Integer::sum));
    }

    void decrementProjectSubscriberCount(String topic) {
        parseProjectIdFromModifyTopic(topic).ifPresent(projectId -> {
            Integer count = projectSubscriberCounts.compute(projectId, (id, current) -> {
                if (current == null || current <= 1) {
                    return null;
                }
                return current - 1;
            });
            if (count == null) {
                collaborationService.evictSession(projectId);
            }
        });
    }

    static Optional<String> parseProjectIdFromModifyTopic(String topic) {
        if (topic == null || !topic.startsWith(MODIFY_TOPIC_PREFIX)) {
            return Optional.empty();
        }
        String remainder = topic.substring(MODIFY_TOPIC_PREFIX.length());
        int slashIndex = remainder.indexOf('/');
        if (slashIndex < 0) {
            return Optional.empty();
        }
        String encodedProjectId = remainder.substring(0, slashIndex);
        return Optional.of(URLDecoder.decode(encodedProjectId, StandardCharsets.UTF_8));
    }
}
