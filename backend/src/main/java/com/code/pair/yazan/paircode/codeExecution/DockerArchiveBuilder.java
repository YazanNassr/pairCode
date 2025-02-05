package com.code.pair.yazan.paircode.codeExecution;

import com.code.pair.yazan.paircode.domain.ProjectFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Builds tar archives for Docker PUT /containers/{id}/archive uploads.
 */
public final class DockerArchiveBuilder {

    private DockerArchiveBuilder() {
    }

    public static byte[] buildTar(List<ProjectFile> files) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tarOutputStream = new TarArchiveOutputStream(outputStream)) {
            tarOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
            for (ProjectFile file : files) {
                String filePath = file.getFilePath();
                ExecutionPathValidator.validateRelativePath(filePath);

                String entryName = normalizeEntryName(filePath);
                byte[] content = file.getSourceCode() != null
                        ? file.getSourceCode().getBytes(StandardCharsets.UTF_8)
                        : new byte[0];

                TarArchiveEntry entry = new TarArchiveEntry(entryName);
                entry.setSize(content.length);
                tarOutputStream.putArchiveEntry(entry);
                tarOutputStream.write(content);
                tarOutputStream.closeArchiveEntry();
            }
            tarOutputStream.finish();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to build Docker archive", e);
        }
        return outputStream.toByteArray();
    }

    private static String normalizeEntryName(String path) {
        if (path.startsWith("./")) {
            return path.substring(2);
        }
        return path;
    }
}
