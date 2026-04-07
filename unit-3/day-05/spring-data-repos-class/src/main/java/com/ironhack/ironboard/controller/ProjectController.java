package com.ironhack.ironboard.controller;

import com.ironhack.ironboard.dto.request.CreateProjectRequest;
import com.ironhack.ironboard.dto.request.UpdateProjectRequest;
import com.ironhack.ironboard.dto.response.ProjectSummary;
import com.ironhack.ironboard.dto.response.ProjectResponse;
import com.ironhack.ironboard.mapper.ProjectMapper;
import com.ironhack.ironboard.entity.Project;
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

import java.util.List;

// =============================================
// PROJECT CONTROLLER -- Day 8: DTOs & Mappers
// =============================================
// CHANGED from Day 7:
// 1. Methods now accept request DTOs (CreateProjectRequest,
//    UpdateProjectRequest) instead of the Project model.
// 2. Methods now return response DTOs instead of Project models.
// 3. ProjectMapper converts between models and DTOs in all directions.
//
// WHY: The controller is the boundary between the HTTP layer
// and the business logic. It receives request DTOs, converts
// them to models via the mapper, calls the service, and converts
// the returned model to a response DTO using the mapper.
// The client never sees the internal domain model.
//
// PATTERN: Every write endpoint follows the same flow:
//   1. Client sends JSON -> Spring deserializes to request DTO
//   2. @Valid validates the DTO
//   3. Controller calls ProjectMapper.toModel(request) -> Project
//   4. Controller passes the model to the service
//   5. Service does business logic and returns the model
//   6. Controller calls ProjectMapper.toResponse(project) -> DTO
//
// TIP: toModel() is overloaded in the mapper:
//   toModel(CreateProjectRequest) -- for POST
//   toModel(UpdateProjectRequest) -- for PUT and PATCH
// Java picks the right version based on the argument type.
//
// COMMON MISTAKE: Forgetting to convert models to DTOs in one
// endpoint but not others. ALL endpoints that return data should
// use the mapper. Inconsistency means some endpoints expose
// internal fields and others don't.
// =============================================
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    // Constructor injection -- Spring provides the ProjectService bean automatically
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // =============================================
    // GET /api/projects
    // GET /api/projects?name=Iron
    // =============================================
    // CHANGED: Return type is now List<ProjectSummary>.
    // ProjectSummary is a lightweight DTO with only id and name --
    // the client doesn't need description or createdAt when
    // browsing a list. Use GET /api/projects/{id} for full detail.
    //
    // The mapper's toSummaryList() centralizes the list conversion
    // so the controller doesn't need its own for-loop.
    // =============================================
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

    // =============================================
    // GET /api/projects/{id}
    // CHANGED: Return type is now ProjectResponse (full detail).
    // For single models, just call the mapper directly.
    // =============================================
    @GetMapping("/{id}")
    public ProjectResponse getProjectById(@PathVariable Long id) {
        Project project = projectService.findById(id);
        return ProjectMapper.toResponse(project);
    }

    // =============================================
    // POST /api/projects
    // CHANGED: Uses ProjectMapper.toModel(CreateProjectRequest) to
    // convert the request DTO to a model, then passes it to the service.
    //
    // PATTERN: The mapper handles DTO-to-model conversion.
    // The service handles internal fields (id, createdAt).
    // Neither the controller nor the mapper sets those fields.
    //
    // NOTE: @Valid validates the CreateProjectRequest DTO
    // (which has @NotBlank on name) before toModel() is called.
    // =============================================
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {
        Project project = ProjectMapper.toModel(request);
        Project created = projectService.create(project);
        ProjectResponse response = ProjectMapper.toResponse(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =============================================
    // PUT /api/projects/{id} -- Full update
    // CHANGED: Uses ProjectMapper.toModel(UpdateProjectRequest) to
    // convert the request DTO to a model, then passes it to the service.
    //
    // PATTERN: Same flow as POST -- toModel() converts DTO to model,
    // the service applies the changes. The only difference is that
    // fullUpdate() replaces ALL fields unconditionally.
    // =============================================
    @PutMapping("/{id}")
    public ProjectResponse updateProject(@PathVariable Long id,
                                         @Valid @RequestBody UpdateProjectRequest request) {
        Project updates = ProjectMapper.toModel(request);
        Project project = projectService.fullUpdate(id, updates);
        return ProjectMapper.toResponse(project);
    }

    // =============================================
    // PATCH /api/projects/{id} -- Partial update
    // CHANGED: Same mapper pattern as PUT.
    //
    // NOTE: @Valid is safe here because UpdateProjectRequest has
    // @Size only (no @NotBlank). Null fields pass validation,
    // which is correct for partial updates.
    //
    // TIP: The mapper copies null fields as-is. The service's
    // partialUpdate() skips null fields (doesn't overwrite).
    // Compare: fullUpdate() overwrites ALL fields, even null ones.
    // =============================================
    @PatchMapping("/{id}")
    public ProjectResponse patchProject(@PathVariable Long id,
                                        @Valid @RequestBody UpdateProjectRequest request) {
        Project updates = ProjectMapper.toModel(request);
        Project project = projectService.partialUpdate(id, updates);
        return ProjectMapper.toResponse(project);
    }

    // DELETE /api/projects/{id} -- No change (returns void with 204)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
