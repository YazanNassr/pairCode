package com.code.pair.yazan.paircode.service.impl;

import com.code.pair.yazan.paircode.domain.Project;
import com.code.pair.yazan.paircode.dsa.InMemoryProject;
import com.code.pair.yazan.paircode.dsa.TextModification;
import com.code.pair.yazan.paircode.exception.InvalidRequestException;
import com.code.pair.yazan.paircode.service.EditorSessionCache;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store of active editor sessions keyed by Project id.
 */
@Component
public class EditorSessionCacheImpl implements EditorSessionCache {

    private final Map<String, InMemoryProject> sessions = new ConcurrentHashMap<>();

    @Override
    public boolean isLoaded(String projectId) {
        return sessions.containsKey(projectId);
    }

    @Override
    public void loadSnapshot(String projectId, Project snapshot) {
        sessions.put(projectId, new InMemoryProject(snapshot));
    }

    @Override
    public Optional<Project> getSnapshotIfLoaded(String projectId) {
        InMemoryProject session = sessions.get(projectId);
        if (session == null) {
            return Optional.empty();
        }
        return Optional.of(session.toProject());
    }

    @Override
    public Project requireSnapshot(String projectId) {
        InMemoryProject session = sessions.get(projectId);
        if (session == null) {
            throw new InvalidRequestException("Editor session not available for project: " + projectId);
        }
        return session.toProject();
    }

    @Override
    public TextModification readFile(String projectId, String filePath) {
        return requireSession(projectId).getFileContent(filePath);
    }

    @Override
    public List<TextModification> applyModifications(
            String projectId,
            String filePath,
            List<TextModification> modifications) {
        for (TextModification mod : modifications) {
            mod.setFilePath(filePath);
        }
        return requireSession(projectId).applyModification(modifications);
    }

    @Override
    public void evictSession(String projectId) {
        sessions.remove(projectId);
    }

    private InMemoryProject requireSession(String projectId) {
        InMemoryProject session = sessions.get(projectId);
        if (session == null) {
            throw new InvalidRequestException("Editor session not available for project: " + projectId);
        }
        return session;
    }
}
