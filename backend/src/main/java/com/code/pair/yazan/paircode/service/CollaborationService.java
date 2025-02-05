package com.code.pair.yazan.paircode.service;

import com.code.pair.yazan.paircode.domain.Project;
import com.code.pair.yazan.paircode.dsa.TextModification;

import java.util.List;
import java.util.Set;

/**
 * Manages in-memory editor sessions for live collaboration (see CONTEXT.md Editor session).
 */
public interface CollaborationService {

    /**
     * Loads the editor session from the database snapshot and records personal list access on first load.
     *
     * @param projectId the Project id
     * @param userId    the authenticated User
     */
    void loadSession(String projectId, String userId);

    /**
     * Reads file content from the editor session.
     *
     * @param projectId the Project id
     * @param filePath  the File path
     * @param userId    the authenticated User
     * @return initial read payload as a {@link TextModification}
     */
    TextModification readFile(String projectId, String filePath, String userId);

    /**
     * Applies collaborative edits and returns the transformed modifications to broadcast.
     *
     * @param projectId     the Project id
     * @param filePath      the File path
     * @param modifications incoming edits
     * @param userId        the authenticated User
     * @return applied modifications
     */
    List<TextModification> applyModifications(
            String projectId,
            String filePath,
            List<TextModification> modifications,
            String userId);

    /**
     * Persists the editor session to the database snapshot (Save).
     *
     * @param projectId the Project id
     * @param userId    the authenticated User
     */
    void saveSession(String projectId, String userId);

    /**
     * Removes the in-memory session without persisting.
     *
     * @param projectId the Project id
     */
    void evictSession(String projectId);

    /**
     * Returns the in-memory snapshot when a session is loaded, otherwise the Mongo snapshot.
     *
     * @param projectId the Project id
     * @param userId    the authenticated User
     * @return Project content to execute
     */
    Project getExecutionSnapshot(String projectId, String userId);

    /**
     * @param topic STOMP topic for modify subscriptions
     * @return usernames of active subscribers
     */
    Set<String> getActiveUsers(String topic);
}
