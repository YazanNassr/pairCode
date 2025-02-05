package com.code.pair.yazan.paircode.domain;

import lombok.*;

/**
 * A source File within a Project (see CONTEXT.md).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectFile {
    @Builder.Default
    private String parentPath = ".";
    private String fileName;
    private String sourceCode;

    /**
     * @return path relative to project root
     */
    public String getFilePath() {
        if (fileName == null) {
            return null;
        }
        return getParentPath() + "/" + fileName;
    }
}
