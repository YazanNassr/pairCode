package com.code.pair.yazan.paircode.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

/**
 * One-time migration from legacy {@code activeProject} collection to {@code project}.
 */
@Slf4j
@Component
@AllArgsConstructor
public class ProjectCollectionMigration {

    private static final String LEGACY_COLLECTION = "activeProject";
    private static final String TARGET_COLLECTION = "project";

    private final MongoTemplate mongoTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void migrateIfNeeded() {
        if (!mongoTemplate.collectionExists(LEGACY_COLLECTION)) {
            return;
        }
        if (mongoTemplate.getCollection(TARGET_COLLECTION).countDocuments() > 0) {
            log.info("Skipping project collection migration: '{}' already populated", TARGET_COLLECTION);
            return;
        }
        long legacyCount = mongoTemplate.getCollection(LEGACY_COLLECTION).countDocuments();
        if (legacyCount == 0) {
            return;
        }

        for (Document document : mongoTemplate.getCollection(LEGACY_COLLECTION).find()) {
            mongoTemplate.getCollection(TARGET_COLLECTION).insertOne(document);
        }
        log.info("Migrated {} documents from '{}' to '{}'", legacyCount, LEGACY_COLLECTION, TARGET_COLLECTION);
    }
}
