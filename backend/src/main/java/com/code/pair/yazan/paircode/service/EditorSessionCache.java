package com.code.pair.yazan.paircode.service;

import com.code.pair.yazan.paircode.domain.Project;
import com.code.pair.yazan.paircode.dsa.TextModification;

import java.util.List;
import java.util.Optional;

/**
 * Tracks in-memory editor sessions for loaded Projects.
 */
public interface EditorSessionCache {

    /**
     * @param projectId the Project id
     * @return true when a session is in memory
     */
    boolean isLoaded(String projectId);

    /**
     * Loads or replaces the in-memory session from a persisted snapshot.
     *
     * @param projectId the Project id
     * @param snapshot  the persisted Project snapshot
     */
    void loadSnapshot(String projectId, Project snapshot);

    /**
     * @param projectId the Project id
     * @return the in-memory snapshot when loaded
     */
    Optional<Project> getSnapshotIfLoaded(String projectId);

    /**
     * @param projectId the Project id
     * @return the in-memory snapshot
     * @throws com.code.pair.yazan.paircode.exception.InvalidRequestException when not loaded
     */
    Project requireSnapshot(String projectId);

    /**
     * Reads file content from the in-memory session.
     *
     * @param projectId the Project id
     * @param filePath  the File path
     * @return read payload as a {@link TextModification}
     * @throws com.code.pair.yazan.paircode.exception.InvalidRequestException when session or file is missing
     */
    TextModification readFile(String projectId, String filePath);

    /**
     * Applies collaborative edits in the in-memory session.
     *
     * @param projectId     the Project id
     * @param filePath      the File path
     * @param modifications incoming edits
     * @return applied modifications
     * @throws com.code.pair.yazan.paircode.exception.InvalidRequestException when session or file is missing
     */
    List<TextModification> applyModifications(
            String projectId,
            String filePath,
            List<TextModification> modifications);

    /**
     * Removes the in-memory session without persisting.
     *
     * @param projectId the Project id
     */
    void evictSession(String projectId);
}
