package com.code.pair.yazan.paircode.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Records that a User has a Project on their personal project list (see CONTEXT.md).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "project_access")
@CompoundIndex(name = "user_project", def = "{'userId': 1, 'projectId': 1}", unique = true)
public class ProjectAccess {
    @Id
    private String id;
    private String userId;
    private String projectId;
    @Builder.Default
    private Instant accessedAt = Instant.now();
    @Builder.Default
    private boolean removed = false;
}
