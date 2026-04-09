package com.ironhack.ironboard.controller;

import com.ironhack.ironboard.dto.request.CreateConsultingProjectRequest;
import com.ironhack.ironboard.dto.request.CreateDevelopmentProjectRequest;
import com.ironhack.ironboard.dto.request.CreateProjectRequest;
import com.ironhack.ironboard.dto.request.FullUpdateProjectRequest;
import com.ironhack.ironboard.dto.request.PatchUpdateProjectRequest;
import com.ironhack.ironboard.dto.response.ProjectSummary;
import com.ironhack.ironboard.dto.response.ProjectResponse;
import com.ironhack.ironboard.entity.ConsultingProject;
import com.ironhack.ironboard.entity.DevelopmentProject;
import com.ironhack.ironboard.entity.Project;
import com.ironhack.ironboard.mapper.ProjectMapper;
import com.ironhack.ironboard.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // GET /api/projects or GET /api/projects?name=Iron
    @GetMapping
    public List<ProjectSummary> getProjects(@RequestParam(required = false) String name) {
        List<Project> projects;
        if (name != null && !name.isBlank()) {
            projects = projectService.findByName(name);
        } else {
            projects = projectService.findAll();
        }

        return ProjectMapper.toSummaryList(projects);
    }

    // GET /api/projects/development
    @GetMapping("/development")
    public List<ProjectSummary> getDevelopmentProjects() {
        List<DevelopmentProject> projects = projectService.findAllDevelopment();
        return ProjectMapper.toSummaryList(new ArrayList<>(projects));
    }

    // GET /api/projects/consulting
    @GetMapping("/consulting")
    public List<ProjectSummary> getConsultingProjects() {
        List<ConsultingProject> projects = projectService.findAllConsulting();
        return ProjectMapper.toSummaryList(new ArrayList<>(projects));
    }

    // GET /api/projects/{id}
    @GetMapping("/{id}")
    public ProjectResponse getProjectById(@PathVariable Long id) {
        Project project = projectService.findById(id);
        return ProjectMapper.toResponse(project);
    }

    // POST /api/projects
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {
        Project project = ProjectMapper.toModel(request);
        Project created = projectService.create(project);
        ProjectResponse response = ProjectMapper.toResponse(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // POST /api/projects/development
    @PostMapping("/development")
    public ResponseEntity<ProjectResponse> createDevelopmentProject(
            @Valid @RequestBody CreateDevelopmentProjectRequest request) {
        DevelopmentProject project = ProjectMapper.toModel(request);
        DevelopmentProject created = projectService.createDevelopment(project);
        ProjectResponse response = ProjectMapper.toResponse(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // POST /api/projects/consulting
    @PostMapping("/consulting")
    public ResponseEntity<ProjectResponse> createConsultingProject(
            @Valid @RequestBody CreateConsultingProjectRequest request) {
        ConsultingProject project = ProjectMapper.toModel(request);
        ConsultingProject created = projectService.createConsulting(project);
        ProjectResponse response = ProjectMapper.toResponse(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PUT /api/projects/{id} -- Full update
    @PutMapping("/{id}")
    public ProjectResponse updateProject(@PathVariable Long id,
                                         @Valid @RequestBody FullUpdateProjectRequest request) {
        Project project = projectService.fullUpdate(id, request);
        return ProjectMapper.toResponse(project);
    }

    // PATCH /api/projects/{id} -- Partial update
    @PatchMapping("/{id}")
    public ProjectResponse patchProject(@PathVariable Long id,
                                        @Valid @RequestBody PatchUpdateProjectRequest request) {
        Project project = projectService.partialUpdate(id, request);
        return ProjectMapper.toResponse(project);
    }

    // DELETE /api/projects/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
