package com.code.pair.yazan.paircode.codeExecution;

import com.code.pair.yazan.paircode.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExecutionPathValidatorTest {

    @Test
    void validateRelativePath_acceptsSafePaths() {
        assertDoesNotThrow(() -> ExecutionPathValidator.validateRelativePath("./main.py"));
        assertDoesNotThrow(() -> ExecutionPathValidator.validateRelativePath("src/app.js"));
        assertDoesNotThrow(() -> ExecutionPathValidator.validateRelativePath("input.txt"));
    }

    @Test
    void validateRelativePath_rejectsParentTraversal() {
        assertThrows(InvalidRequestException.class,
                () -> ExecutionPathValidator.validateRelativePath("../etc/passwd"));
    }

    @Test
    void validateRelativePath_rejectsAbsolutePaths() {
        assertThrows(InvalidRequestException.class,
                () -> ExecutionPathValidator.validateRelativePath("/etc/passwd"));
    }

    @Test
    void validateRelativePath_rejectsShellMetacharacters() {
        assertThrows(InvalidRequestException.class,
                () -> ExecutionPathValidator.validateRelativePath("main.py; rm -rf /"));
        assertThrows(InvalidRequestException.class,
                () -> ExecutionPathValidator.validateRelativePath("$(whoami).py"));
        assertThrows(InvalidRequestException.class,
                () -> ExecutionPathValidator.validateRelativePath("`id`.py"));
    }

    @Test
    void validateRelativePath_rejectsEmptyPath() {
        assertThrows(InvalidRequestException.class,
                () -> ExecutionPathValidator.validateRelativePath(""));
    }
}
