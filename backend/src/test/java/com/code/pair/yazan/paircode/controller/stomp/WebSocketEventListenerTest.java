package com.code.pair.yazan.paircode.controller.stomp;

import com.code.pair.yazan.paircode.service.CollaborationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebSocketEventListenerTest {

    private static final String MODIFY_TOPIC = "/topic/modify/p1/./main.py";

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private CollaborationService collaborationService;

    @InjectMocks
    private WebSocketEventListener webSocketEventListener;

    @Test
    void parseProjectIdFromModifyTopic_decodesEncodedProjectId() {
        Optional<String> projectId = WebSocketEventListener.parseProjectIdFromModifyTopic(
                "/topic/modify/p1%2Fextra/./main.py");

        assertTrue(projectId.isPresent());
        assertEquals("p1/extra", projectId.get());
    }

    @Test
    void parseProjectIdFromModifyTopic_returnsEmptyForNonModifyTopic() {
        assertTrue(WebSocketEventListener.parseProjectIdFromModifyTopic("/topic/read/p1/file/u").isEmpty());
    }

    @Test
    void decrementProjectSubscriberCount_evictsWhenLastSubscriberLeaves() {
        webSocketEventListener.incrementProjectSubscriberCount(MODIFY_TOPIC);
        webSocketEventListener.decrementProjectSubscriberCount(MODIFY_TOPIC);

        verify(collaborationService).evictSession("p1");
    }

    @Test
    void decrementProjectSubscriberCount_keepsSessionWhileSubscribersRemain() {
        webSocketEventListener.incrementProjectSubscriberCount(MODIFY_TOPIC);
        webSocketEventListener.incrementProjectSubscriberCount(MODIFY_TOPIC);
        webSocketEventListener.decrementProjectSubscriberCount(MODIFY_TOPIC);

        verify(collaborationService, never()).evictSession("p1");
    }

    @Test
    void handleSessionSubscribeEvent_tracksMultipleSubscribersOnSameTopic() {
        webSocketEventListener.handleSessionSubscribeEvent(
                subscribeEvent("session-1", "alice", MODIFY_TOPIC));
        webSocketEventListener.handleSessionSubscribeEvent(
                subscribeEvent("session-2", "bob", MODIFY_TOPIC));

        Set<String> subscribers = webSocketEventListener.getSubscribers(MODIFY_TOPIC);
        assertEquals(2, subscribers.size());
        assertTrue(subscribers.contains("alice"));
        assertTrue(subscribers.contains("bob"));
    }

    private SessionSubscribeEvent subscribeEvent(String sessionId, String username, String topic) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setSessionId(sessionId);
        accessor.setDestination(topic);
        accessor.setLeaveMutable(true);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        Principal principal = () -> username;
        return new SessionSubscribeEvent(this, message, principal);
    }
}
