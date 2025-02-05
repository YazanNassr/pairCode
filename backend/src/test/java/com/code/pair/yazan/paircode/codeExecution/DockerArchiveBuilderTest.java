package com.code.pair.yazan.paircode.codeExecution;

import com.code.pair.yazan.paircode.domain.ProjectFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DockerArchiveBuilderTest {

    @Test
    void buildTar_containsExpectedEntriesWithUtf8Content() throws IOException {
        List<ProjectFile> files = List.of(
                ProjectFile.builder()
                        .parentPath(".")
                        .fileName("main.py")
                        .sourceCode("print('hi')")
                        .build(),
                ProjectFile.builder()
                        .parentPath("src")
                        .fileName("util.py")
                        .sourceCode("x = 1")
                        .build()
        );

        byte[] archive = DockerArchiveBuilder.buildTar(files);
        Map<String, String> entries = readTarEntries(archive);

        assertEquals(2, entries.size());
        assertEquals("print('hi')", entries.get("main.py"));
        assertEquals("x = 1", entries.get("src/util.py"));
    }

    private Map<String, String> readTarEntries(byte[] archive) throws IOException {
        Map<String, String> entries = new LinkedHashMap<>();
        try (TarArchiveInputStream tarInputStream = new TarArchiveInputStream(new ByteArrayInputStream(archive))) {
            TarArchiveEntry entry;
            while ((entry = tarInputStream.getNextEntry()) != null) {
                byte[] content = tarInputStream.readAllBytes();
                entries.put(entry.getName(), new String(content, StandardCharsets.UTF_8));
            }
        }
        return entries;
    }
}
