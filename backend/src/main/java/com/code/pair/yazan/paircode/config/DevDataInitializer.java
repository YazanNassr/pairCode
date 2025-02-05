package com.code.pair.yazan.paircode.config;

import com.code.pair.yazan.paircode.domain.Project;
import com.code.pair.yazan.paircode.domain.AppUser;
import com.code.pair.yazan.paircode.domain.ProjectAccess;
import com.code.pair.yazan.paircode.repository.ProjectRepository;
import com.code.pair.yazan.paircode.repository.AppUserRepository;
import com.code.pair.yazan.paircode.repository.ProjectAccessRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Dev/demo seed data and one-time owner {@link ProjectAccess} backfill for existing projects.
 */
@Component
@Profile("dev")
@AllArgsConstructor
public class DevDataInitializer {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProjectRepository projectRepository;
    private final ProjectAccessRepository projectAccessRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        seedDemoUsers();
        backfillOwnerAccess();
    }

    private void seedDemoUsers() {
        if (appUserRepository.findByUsername("user1").isEmpty()) {
            appUserRepository.save(new AppUser("user1", passwordEncoder.encode("123")));
        }
        if (appUserRepository.findByUsername("user2").isEmpty()) {
            appUserRepository.save(new AppUser("user2", passwordEncoder.encode("123")));
        }
    }

    private void backfillOwnerAccess() {
        for (Project project : projectRepository.findAll()) {
            upsertAccess(project.getOwnerId(), project.getId());
        }
    }

    private void upsertAccess(String userId, String projectId) {
        projectAccessRepository.findByUserIdAndProjectId(userId, projectId)
                .ifPresentOrElse(
                        access -> {
                            if (access.isRemoved()) {
                                access.setRemoved(false);
                                access.setAccessedAt(Instant.now());
                                projectAccessRepository.save(access);
                            }
                        },
                        () -> projectAccessRepository.save(ProjectAccess.builder()
                                .userId(userId)
                                .projectId(projectId)
                                .accessedAt(Instant.now())
                                .removed(false)
                                .build()));
    }
}
