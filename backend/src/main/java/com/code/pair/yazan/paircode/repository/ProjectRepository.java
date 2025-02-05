package com.code.pair.yazan.paircode.repository;

import com.code.pair.yazan.paircode.domain.Project;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Persistence for {@link Project} documents.
 */
public interface ProjectRepository extends CrudRepository<Project, String> {
    List<Project> findAllByOwnerId(String ownerId);
}
