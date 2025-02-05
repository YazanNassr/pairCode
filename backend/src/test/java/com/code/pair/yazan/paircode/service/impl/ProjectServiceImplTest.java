package com.code.pair.yazan.paircode.service.impl;

import com.code.pair.yazan.paircode.domain.ProjectFile;
import com.code.pair.yazan.paircode.domain.Project;
import com.code.pair.yazan.paircode.domain.ProjectAccess;
import com.code.pair.yazan.paircode.exception.AccessDeniedException;
import com.code.pair.yazan.paircode.exception.ProjectConflictException;
import com.code.pair.yazan.paircode.exception.ProjectNotFoundException;
import com.code.pair.yazan.paircode.repository.ProjectRepository;
import com.code.pair.yazan.paircode.repository.ProjectAccessRepository;
import com.code.pair.yazan.paircode.service.CollaborationService;
import com.code.pair.yazan.paircode.service.EditorSessionCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectAccessRepository projectAccessRepository;

    @Mock
    private EditorSessionCache editorSessionCache;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private Project project;

    @BeforeEach
    void setUp() {
        project = Project.builder()
                .id("p1")
                .name("Demo")
                .ownerId("owner")
                .files(List.of(ProjectFile.builder().fileName("main.py").sourceCode("print(1)").build()))
                .build();
    }

    @Test
    void listForUser_returnsProjectsFromAccessRows() {
        // Given
        ProjectAccess access = ProjectAccess.builder().userId("user1").projectId("p1").removed(false).build();
        when(projectAccessRepository.findByUserIdAndRemovedFalse("user1")).thenReturn(List.of(access));
        when(projectRepository.findById("p1")).thenReturn(Optional.of(project));

        // When
        List<Project> result = projectService.listForUser("user1");

        // Then
        assertEquals(1, result.size());
        assertEquals("p1", result.getFirst().getId());
    }

    @Test
    void getById_whenMissing_throwsNotFound() {
        when(projectRepository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(ProjectNotFoundException.class, () -> projectService.getById("missing", "user1"));
    }

    @Test
    void create_setsOwnerAndRecordsAccess() {
        Project toCreate = Project.builder().name("New").build();
        when(projectRepository.save(any())).thenAnswer(inv -> {
            Project saved = inv.getArgument(0);
            saved.setId("new-id");
            return saved;
        });
        when(projectRepository.findById("new-id")).thenAnswer(inv -> Optional.of(
                Project.builder().id("new-id").name("New").ownerId("owner").build()));
        when(projectAccessRepository.findByUserIdAndProjectId("owner", "new-id")).thenReturn(Optional.empty());

        Project created = projectService.create(toCreate, "owner");

        assertEquals("owner", created.getOwnerId());
        verify(projectAccessRepository).save(any(ProjectAccess.class));
    }

    @Test
    void create_whenIdBlank_clearsIdBeforeSave() {
        Project toCreate = Project.builder().id("").name("New").build();
        when(projectRepository.save(any())).thenAnswer(inv -> {
            Project saved = inv.getArgument(0);
            assertNull(saved.getId());
            saved.setId("new-id");
            return saved;
        });
        when(projectRepository.findById("new-id")).thenReturn(Optional.of(
                Project.builder().id("new-id").name("New").ownerId("owner").build()));
        when(projectAccessRepository.findByUserIdAndProjectId("owner", "new-id")).thenReturn(Optional.empty());

        Project created = projectService.create(toCreate, "owner");

        assertEquals("new-id", created.getId());
        verify(projectAccessRepository).save(any(ProjectAccess.class));
    }

    @Test
    void update_whenCacheHot_throwsConflict() {
        when(projectRepository.findById("p1")).thenReturn(Optional.of(project));
        when(editorSessionCache.isLoaded("p1")).thenReturn(true);

        assertThrows(ProjectConflictException.class, () -> projectService.update(project, "owner"));
    }

    @Test
    void update_whenGuest_throwsAccessDenied() {
        when(projectRepository.findById("p1")).thenReturn(Optional.of(project));

        assertThrows(AccessDeniedException.class, () -> projectService.update(project, "guest"));
    }

    @Test
    void deleteProject_whenOwner_evictsCacheAndDeletesAccess() {
        when(projectRepository.findById("p1")).thenReturn(Optional.of(project));

        projectService.deleteProject("p1", "owner");

        verify(editorSessionCache).evictSession("p1");
        verify(projectAccessRepository).deleteAllByProjectId("p1");
        verify(projectRepository).deleteById("p1");
    }

    @Test
    void removeFromList_whenGuest_marksAccessRemoved() {
        when(projectRepository.findById("p1")).thenReturn(Optional.of(project));
        ProjectAccess access = ProjectAccess.builder().userId("guest").projectId("p1").removed(false).build();
        when(projectAccessRepository.findByUserIdAndProjectId("guest", "p1")).thenReturn(Optional.of(access));

        projectService.removeFromList("p1", "guest");

        assertTrue(access.isRemoved());
        verify(projectAccessRepository).save(access);
        verify(projectRepository, never()).deleteById(any());
    }

    @Test
    void removeFromList_whenOwner_hardDeletes() {
        when(projectRepository.findById("p1")).thenReturn(Optional.of(project));

        projectService.removeFromList("p1", "owner");

        verify(projectRepository).deleteById("p1");
    }

    @Test
    void recordAccess_upsertsAccessRow() {
        when(projectRepository.findById("p1")).thenReturn(Optional.of(project));
        when(projectAccessRepository.findByUserIdAndProjectId("guest", "p1")).thenReturn(Optional.empty());

        projectService.recordAccess("p1", "guest");

        verify(projectAccessRepository).save(any(ProjectAccess.class));
    }

    @Test
    void recordAccess_whenPreviouslyRemoved_clearsRemovedFlag() {
        when(projectRepository.findById("p1")).thenReturn(Optional.of(project));
        ProjectAccess access = ProjectAccess.builder().userId("guest").projectId("p1").removed(true).build();
        when(projectAccessRepository.findByUserIdAndProjectId("guest", "p1")).thenReturn(Optional.of(access));

        projectService.recordAccess("p1", "guest");

        assertFalse(access.isRemoved());
        verify(projectAccessRepository).save(access);
    }
}
