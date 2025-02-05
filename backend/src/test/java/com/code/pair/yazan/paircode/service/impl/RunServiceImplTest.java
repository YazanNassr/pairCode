package com.code.pair.yazan.paircode.service.impl;

import com.code.pair.yazan.paircode.domain.ProjectFile;
import com.code.pair.yazan.paircode.domain.Project;
import com.code.pair.yazan.paircode.exception.InvalidRequestException;
import com.code.pair.yazan.paircode.service.CollaborationService;
import com.code.pair.yazan.paircode.service.DockerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RunServiceImplTest {

    @Mock
    private CollaborationService collaborationService;

    @Mock
    private DockerService dockerService;

    @InjectMocks
    private RunServiceImpl runService;

    @Test
    void run_doesNotMutatePersistedProjectFiles() {
        // Given
        List<ProjectFile> persistedFiles = new ArrayList<>();
        persistedFiles.add(ProjectFile.builder().fileName("main.py").sourceCode("original").build());
        Project project = Project.builder()
                .id("p1")
                .ownerId("owner")
                .files(persistedFiles)
                .build();
        when(collaborationService.getExecutionSnapshot("p1", "owner")).thenReturn(project);
        when(dockerService.runPythonCode(any(), eq("./input.txt"), eq("./main.py"))).thenReturn("ok");

        // When
        String output = runService.run("p1", "owner", "./main.py", "stdin", "python");

        // Then
        assertEquals("ok", output);
        assertEquals(1, persistedFiles.size());
        assertEquals("original", persistedFiles.getFirst().getSourceCode());

        ArgumentCaptor<List<ProjectFile>> filesCaptor = ArgumentCaptor.forClass(List.class);
        verify(dockerService).runPythonCode(filesCaptor.capture(), eq("./input.txt"), eq("./main.py"));
        assertEquals(2, filesCaptor.getValue().size());
    }

    @Test
    void run_whenCacheHot_usesInMemoryFiles() {
        Project hotProject = Project.builder()
                .id("p1")
                .ownerId("owner")
                .files(List.of(ProjectFile.builder().fileName("main.py").sourceCode("live").build()))
                .build();
        when(collaborationService.getExecutionSnapshot("p1", "owner")).thenReturn(hotProject);
        when(dockerService.runPythonCode(any(), eq("./input.txt"), eq("./main.py"))).thenReturn("ok");

        runService.run("p1", "owner", "./main.py", "stdin", "python");

        ArgumentCaptor<List<ProjectFile>> filesCaptor = ArgumentCaptor.forClass(List.class);
        verify(dockerService).runPythonCode(filesCaptor.capture(), eq("./input.txt"), eq("./main.py"));
        assertEquals("live", filesCaptor.getValue().getFirst().getSourceCode());
    }

    @Test
    void run_whenCacheCold_usesMongoFiles() {
        Project coldProject = Project.builder()
                .id("p1")
                .ownerId("owner")
                .files(List.of(ProjectFile.builder().fileName("main.py").sourceCode("saved").build()))
                .build();
        when(collaborationService.getExecutionSnapshot("p1", "owner")).thenReturn(coldProject);
        when(dockerService.runPythonCode(any(), eq("./input.txt"), eq("./main.py"))).thenReturn("ok");

        runService.run("p1", "owner", "./main.py", "stdin", "python");

        ArgumentCaptor<List<ProjectFile>> filesCaptor = ArgumentCaptor.forClass(List.class);
        verify(dockerService).runPythonCode(filesCaptor.capture(), eq("./input.txt"), eq("./main.py"));
        assertEquals("saved", filesCaptor.getValue().getFirst().getSourceCode());
    }

    @Test
    void run_withInvalidLanguage_throws() {
        Project project = Project.builder().id("p1").ownerId("owner").files(List.of()).build();
        when(collaborationService.getExecutionSnapshot("p1", "owner")).thenReturn(project);

        assertThrows(InvalidRequestException.class,
                () -> runService.run("p1", "owner", "./main.py", "", "ruby"));
    }

    @Test
    void run_withInvalidMainFilePath_throwsBeforeDocker() {
        assertThrows(InvalidRequestException.class,
                () -> runService.run("p1", "owner", "../etc/passwd", "", "python"));
    }
}
