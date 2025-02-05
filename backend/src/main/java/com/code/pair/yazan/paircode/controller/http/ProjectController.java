package com.code.pair.yazan.paircode.controller.http;

import com.code.pair.yazan.paircode.domain.Project;
import com.code.pair.yazan.paircode.service.ProjectService;
import com.code.pair.yazan.paircode.service.RunService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * HTTP API for persisted {@linkplain Project Project} operations.
 */
@RestController
@RequestMapping("/project")
@AllArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final RunService runService;

    @GetMapping("/all")
    public List<Project> getAll(Principal principal) {
        return projectService.listForUser(principal.getName());
    }

    @GetMapping("/{id}")
    public Project getById(@PathVariable String id, Principal principal) {
        return projectService.getById(id, principal.getName());
    }

    @PostMapping
    public Project create(@RequestBody Project project, Principal principal) {
        return projectService.create(project, principal.getName());
    }

    @PutMapping
    public Project update(@RequestBody Project project, Principal principal) {
        return projectService.update(project, principal.getName());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id, Principal principal) {
        projectService.removeFromList(id, principal.getName());
    }

    public record RunInfo(String mainFilePath, String input, String language) {
    }

    @PostMapping("/{id}/run")
    public String run(@PathVariable String id, @RequestBody RunInfo runInfo, Principal principal) {
        return runService.run(
                id,
                principal.getName(),
                runInfo.mainFilePath(),
                runInfo.input(),
                runInfo.language());
    }
}
