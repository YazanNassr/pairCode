package com.code.pair.yazan.paircode.service.impl;

import com.code.pair.yazan.paircode.domain.ProjectFile;
import com.code.pair.yazan.paircode.domain.Project;
import com.code.pair.yazan.paircode.dsa.TextModification;
import com.code.pair.yazan.paircode.service.EditorSessionCache;
import com.code.pair.yazan.paircode.service.PresenceTracker;
import com.code.pair.yazan.paircode.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollaborationServiceImplTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private EditorSessionCache editorSessionCache;

    @Mock
    private PresenceTracker presenceTracker;

    @InjectMocks
    private CollaborationServiceImpl collaborationService;

    private Project project;

    @BeforeEach
    void setUp() {
        project = Project.builder()
                .id("p1")
                .name("Demo")
                .ownerId("owner")
                .files(List.of(ProjectFile.builder().fileName("main.py").sourceCode("x=1").build()))
                .build();
    }

    @Test
    void loadSession_recordsAccessAndLoadsCache() {
        when(projectService.requireExists("p1", "guest")).thenReturn(project);
        when(editorSessionCache.isLoaded("p1")).thenReturn(false);
        when(projectService.getById("p1", "guest")).thenReturn(project);

        collaborationService.loadSession("p1", "guest");

        verify(editorSessionCache).loadSnapshot("p1", project);
        verify(projectService).recordAccess("p1", "guest");
    }

    @Test
    void loadSession_whenAlreadyLoaded_stillRecordsAccess() {
        when(projectService.requireExists(eq("p1"), any())).thenReturn(project);
        when(editorSessionCache.isLoaded("p1")).thenReturn(true);

        collaborationService.loadSession("p1", "owner");
        collaborationService.loadSession("p1", "guest");

        verify(projectService).recordAccess("p1", "owner");
        verify(projectService).recordAccess("p1", "guest");
        verify(editorSessionCache, never()).loadSnapshot(any(), any());
    }

    @Test
    void saveSession_persistsSnapshot() {
        when(projectService.requireExists("p1", "owner")).thenReturn(project);
        when(editorSessionCache.isLoaded("p1")).thenReturn(true);
        when(editorSessionCache.requireSnapshot("p1")).thenReturn(project);

        collaborationService.saveSession("p1", "owner");

        verify(projectService).persistProjectSnapshot(project, "owner");
    }

    @Test
    void readFile_returnsContentModification() {
        TextModification readPayload = TextModification.builder().newVal("x=1").build();
        when(projectService.requireExists("p1", "owner")).thenReturn(project);
        when(editorSessionCache.isLoaded("p1")).thenReturn(true);
        when(editorSessionCache.readFile("p1", "./main.py")).thenReturn(readPayload);

        TextModification result = collaborationService.readFile("p1", "./main.py", "owner");

        assertEquals("x=1", result.getNewVal());
    }

    @Test
    void getExecutionSnapshot_whenCacheHot_returnsInMemorySnapshot() {
        Project hotSnapshot = Project.builder().id("p1").ownerId("owner").build();
        when(projectService.requireExists("p1", "owner")).thenReturn(project);
        when(editorSessionCache.isLoaded("p1")).thenReturn(true);
        when(editorSessionCache.requireSnapshot("p1")).thenReturn(hotSnapshot);

        Project result = collaborationService.getExecutionSnapshot("p1", "owner");

        assertEquals(hotSnapshot, result);
        verify(projectService, never()).getById(any(), any());
    }

    @Test
    void getExecutionSnapshot_whenCacheCold_returnsMongoSnapshot() {
        when(projectService.requireExists("p1", "owner")).thenReturn(project);
        when(editorSessionCache.isLoaded("p1")).thenReturn(false);
        when(projectService.getById("p1", "owner")).thenReturn(project);

        Project result = collaborationService.getExecutionSnapshot("p1", "owner");

        assertEquals(project, result);
    }

    @Test
    void getActiveUsers_delegatesToPresenceTracker() {
        when(presenceTracker.getSubscribers("/topic/modify/p1/./main.py"))
                .thenReturn(Set.of("a", "b"));

        Set<String> users = collaborationService.getActiveUsers("/topic/modify/p1/./main.py");

        assertEquals(2, users.size());
    }
}
