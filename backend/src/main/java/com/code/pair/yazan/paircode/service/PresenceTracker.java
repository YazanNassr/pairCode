package com.code.pair.yazan.paircode.service;

import java.util.Set;

/**
 * Tracks active subscribers on STOMP topics for editor presence.
 */
public interface PresenceTracker {

    /**
     * @param topic the STOMP topic destination
     * @return usernames currently subscribed to the topic
     */
    Set<String> getSubscribers(String topic);
}
