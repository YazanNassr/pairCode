package com.code.pair.yazan.paircode;

import com.code.pair.yazan.paircode.repository.ProjectRepository;
import com.code.pair.yazan.paircode.repository.AppUserRepository;
import com.code.pair.yazan.paircode.repository.ProjectAccessRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration"
})
class PaircodeApplicationTests {

    @MockitoBean
    private ProjectRepository projectRepository;

    @MockitoBean
    private ProjectAccessRepository projectAccessRepository;

    @MockitoBean
    private AppUserRepository appUserRepository;

    @Test
    void contextLoads() {
    }
}
