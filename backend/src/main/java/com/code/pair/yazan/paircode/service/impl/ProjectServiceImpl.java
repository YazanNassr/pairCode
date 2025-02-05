package com.code.pair.yazan.paircode.service.impl;

import com.code.pair.yazan.paircode.domain.Project;
import com.code.pair.yazan.paircode.domain.ProjectAccess;
import com.code.pair.yazan.paircode.exception.AccessDeniedException;
import com.code.pair.yazan.paircode.exception.InvalidRequestException;
import com.code.pair.yazan.paircode.exception.ProjectConflictException;
import com.code.pair.yazan.paircode.exception.ProjectNotFoundException;
import com.code.pair.yazan.paircode.repository.ProjectRepository;
import com.code.pair.yazan.paircode.repository.ProjectAccessRepository;
import com.code.pair.yazan.paircode.service.EditorSessionCache;
import com.code.pair.yazan.paircode.service.ProjectService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Default {@link ProjectService} implementation.
 */
@Service
@AllArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectAccessRepository projectAccessRepository;
    private final EditorSessionCache editorSessionCache;

    @Override
    public List<Project> listForUser(String userId) {
        List<ProjectAccess> accessRows = projectAccessRepository.findByUserIdAndRemovedFalse(userId);
        List<Project> projects = new ArrayList<>();
        for (ProjectAccess access : accessRows) {
            projectRepository.findById(access.getProjectId()).ifPresent(projects::add);
        }
        return projects;
    }

    @Override
    public Project getById(String projectId, String userId) {
        return requireExists(projectId, userId);
    }

    @Override
    public Project create(Project project, String userId) {
        if (project.getId() != null && project.getId().isBlank()) {
            project.setId(null);
        }
        project.setOwnerId(userId);
        Project saved = projectRepository.save(project);
        recordAccess(saved.getId(), userId);
        return saved;
    }

    @Override
    public Project update(Project project, String userId) {
        requireOwner(project.getId(), userId);
        if (editorSessionCache.isLoaded(project.getId())) {
            throw new ProjectConflictException("Cannot update project while editor session is active");
        }
        return projectRepository.save(project);
    }

    @Override
    public void deleteProject(String projectId, String userId) {
        requireOwner(projectId, userId);
        editorSessionCache.evictSession(projectId);
        projectAccessRepository.deleteAllByProjectId(projectId);
        projectRepository.deleteById(projectId);
    }

    @Override
    public void removeFromList(String projectId, String userId) {
        if (isOwner(projectId, userId)) {
            deleteProject(projectId, userId);
            return;
        }
        requireExists(projectId, userId);
        ProjectAccess access = projectAccessRepository.findByUserIdAndProjectId(userId, projectId)
                .orElseThrow(() -> new AccessDeniedException("Project is not on your list"));
        access.setRemoved(true);
        projectAccessRepository.save(access);
    }

    @Override
    public void recordAccess(String projectId, String userId) {
        requireExists(projectId, userId);
        projectAccessRepository.findByUserIdAndProjectId(userId, projectId)
                .ifPresentOrElse(
                        access -> {
                            access.setRemoved(false);
                            access.setAccessedAt(Instant.now());
                            projectAccessRepository.save(access);
                        },
                        () -> projectAccessRepository.save(ProjectAccess.builder()
                                .userId(userId)
                                .projectId(projectId)
                                .accessedAt(Instant.now())
                                .removed(false)
                                .build()));
    }

    @Override
    public Project persistProjectSnapshot(Project project, String userId) {
        requireExists(project.getId(), userId);
        return projectRepository.save(project);
    }

    @Override
    public Project requireExists(String projectId, String userId) {
        if (projectId == null || projectId.isBlank()) {
            throw new InvalidRequestException("Project id is required");
        }
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
    }

    @Override
    public Project requireOwner(String projectId, String userId) {
        Project project = requireExists(projectId, userId);
        if (!project.getOwnerId().equals(userId)) {
            throw new AccessDeniedException("Only the project owner may perform this action");
        }
        return project;
    }

    @Override
    public boolean isOwner(String projectId, String userId) {
        return projectRepository.findById(projectId)
                .map(project -> project.getOwnerId().equals(userId))
                .orElse(false);
    }
}
