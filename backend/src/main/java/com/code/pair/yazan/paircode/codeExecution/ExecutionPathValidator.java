package com.code.pair.yazan.paircode.codeExecution;

import com.code.pair.yazan.paircode.exception.InvalidRequestException;

import java.util.regex.Pattern;

/**
 * Validates relative paths used in Docker code execution.
 */
public final class ExecutionPathValidator {

    private static final Pattern SAFE_PATH = Pattern.compile("^[a-zA-Z0-9._/-]+$");

    private ExecutionPathValidator() {
    }

    public static void validateRelativePath(String path) {
        if (path == null || path.isBlank()) {
            throw new InvalidRequestException("Path must not be empty");
        }
        if (path.startsWith("/")) {
            throw new InvalidRequestException("Path must be relative: " + path);
        }
        if (path.contains("..")) {
            throw new InvalidRequestException("Path must not contain ..: " + path);
        }
        if (!SAFE_PATH.matcher(path).matches()) {
            throw new InvalidRequestException("Invalid path characters: " + path);
        }
    }
}
