package com.code.pair.yazan.paircode.service.impl;

import com.code.pair.yazan.paircode.domain.Project;
import com.code.pair.yazan.paircode.dsa.TextModification;
import com.code.pair.yazan.paircode.service.CollaborationService;
import com.code.pair.yazan.paircode.service.EditorSessionCache;
import com.code.pair.yazan.paircode.service.PresenceTracker;
import com.code.pair.yazan.paircode.service.ProjectService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * In-memory editor session cache with collaborative editing.
 */
@Service
@AllArgsConstructor
public class CollaborationServiceImpl implements CollaborationService {

    private final ProjectService projectService;
    private final EditorSessionCache editorSessionCache;
    private final PresenceTracker presenceTracker;

    @Override
    public void loadSession(String projectId, String userId) {
        projectService.requireExists(projectId, userId);
        if (!editorSessionCache.isLoaded(projectId)) {
            editorSessionCache.loadSnapshot(projectId, projectService.getById(projectId, userId));
        }
        projectService.recordAccess(projectId, userId);
    }

    @Override
    public TextModification readFile(String projectId, String filePath, String userId) {
        loadSession(projectId, userId);
        return editorSessionCache.readFile(projectId, filePath);
    }

    @Override
    public List<TextModification> applyModifications(
            String projectId,
            String filePath,
            List<TextModification> modifications,
            String userId) {
        loadSession(projectId, userId);
        return editorSessionCache.applyModifications(projectId, filePath, modifications);
    }

    @Override
    public void saveSession(String projectId, String userId) {
        loadSession(projectId, userId);
        projectService.persistProjectSnapshot(editorSessionCache.requireSnapshot(projectId), userId);
    }

    @Override
    public void evictSession(String projectId) {
        editorSessionCache.evictSession(projectId);
    }

    @Override
    public Project getExecutionSnapshot(String projectId, String userId) {
        projectService.requireExists(projectId, userId);
        if (editorSessionCache.isLoaded(projectId)) {
            return editorSessionCache.requireSnapshot(projectId);
        }
        return projectService.getById(projectId, userId);
    }

    @Override
    public Set<String> getActiveUsers(String topic) {
        return presenceTracker.getSubscribers(topic);
    }
}
