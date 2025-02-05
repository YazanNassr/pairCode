package com.code.pair.yazan.paircode.service;

import com.code.pair.yazan.paircode.domain.Project;

import java.util.List;

/**
 * Application service for persisted {@linkplain com.code.pair.yazan.paircode.domain.Project Project} lifecycle.
 */
public interface ProjectService {

    /**
     * @param userId the authenticated User
     * @return Projects on the User's personal project list
     */
    List<Project> listForUser(String userId);

    /**
     * @param projectId the Project id
     * @param userId    the authenticated User
     * @return the Project if it exists (link-based access)
     * @throws com.code.pair.yazan.paircode.exception.ProjectNotFoundException when missing
     */
    Project getById(String projectId, String userId);

    /**
     * @param project the Project to create (id may be null)
     * @param userId  the creating User
     * @return the persisted Project
     */
    Project create(Project project, String userId);

    /**
     * @param project the Project to update
     * @param userId  the authenticated User
     * @return the updated Project
     * @throws com.code.pair.yazan.paircode.exception.AccessDeniedException when not owner
     * @throws com.code.pair.yazan.paircode.exception.ProjectConflictException when editor session is hot
     */
    Project update(Project project, String userId);

    /**
     * Hard-deletes a Project for all Users (owner only).
     *
     * @param projectId the Project id
     * @param userId    the authenticated User
     */
    void deleteProject(String projectId, String userId);

    /**
     * Removes a Project from the User's list; guests soft-remove, owners hard-delete.
     *
     * @param projectId the Project id
     * @param userId    the authenticated User
     */
    void removeFromList(String projectId, String userId);

    /**
     * Records personal list access (first STOMP session or create).
     *
     * @param projectId the Project id
     * @param userId    the authenticated User
     */
    void recordAccess(String projectId, String userId);

    /**
     * Persists a Project snapshot from an editor session Save.
     *
     * @param project the snapshot to persist
     * @param userId  the authenticated User
     * @return the saved Project
     */
    Project persistProjectSnapshot(Project project, String userId);

    /**
     * Ensures the Project exists for link-based access. The {@code userId} identifies the
     * authenticated caller; it does not enforce membership on a personal list.
     *
     * @param projectId the Project id
     * @param userId    the authenticated User
     * @return the Project
     * @throws com.code.pair.yazan.paircode.exception.ProjectNotFoundException when missing
     */
    Project requireExists(String projectId, String userId);

    /**
     * @param projectId the Project id
     * @param userId    the authenticated User
     * @return the Project
     * @throws com.code.pair.yazan.paircode.exception.AccessDeniedException when not owner
     */
    Project requireOwner(String projectId, String userId);

    /**
     * @param projectId the Project id
     * @param userId    the authenticated User
     * @return true when the User owns the Project
     */
    boolean isOwner(String projectId, String userId);
}
