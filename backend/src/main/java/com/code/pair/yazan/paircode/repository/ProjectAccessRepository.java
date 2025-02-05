package com.code.pair.yazan.paircode.repository;

import com.code.pair.yazan.paircode.domain.ProjectAccess;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

/**
 * Persistence for {@link ProjectAccess} personal list entries.
 */
public interface ProjectAccessRepository extends CrudRepository<ProjectAccess, String> {
    List<ProjectAccess> findByUserIdAndRemovedFalse(String userId);

    Optional<ProjectAccess> findByUserIdAndProjectId(String userId, String projectId);

    void deleteAllByProjectId(String projectId);

    List<ProjectAccess> findByProjectId(String projectId);
}
