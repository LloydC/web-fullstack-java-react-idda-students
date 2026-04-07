package com.ironhack.ironboard.service;

import com.ironhack.ironboard.exception.ResourceNotFoundException;
import com.ironhack.ironboard.entity.Project;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// =============================================
// PROJECT SERVICE — STEP 04: FULL CRUD + EXCEPTIONS
// =============================================
// CHANGED from Step 03 (Day 6): findById() now throws
// ResourceNotFoundException instead of RuntimeException.
// This is the KEY change for exception handling — the
// GlobalExceptionHandler catches ResourceNotFoundException
// and returns a proper 404 JSON response.
//
// PATTERN: "Throw early, handle late"
//   - "Throw early" = throw as soon as you detect the problem
//     (here in findById, right when we see the resource is null)
//   - "Handle late" = handle it at the boundary (GlobalExceptionHandler),
//     not in every intermediate method
//   This keeps the code clean: services throw, controllers propagate,
//   GlobalExceptionHandler catches and formats.
//
// WHY: Before (Day 6): threw RuntimeException
//   → Spring returned generic 500 Internal Server Error (wrong!)
// After (Day 7): throws ResourceNotFoundException
//   → GlobalExceptionHandler catches it → returns 404 Not Found (correct!)
// =============================================
@Service
public class ProjectService {

    // =============================================
    // NOTE: This service still uses HashMap for storage.
    // Even though the entity classes now have JPA annotations,
    // we have NOT created repositories yet. Hibernate creates
    // the database tables on startup (check MySQL Workbench),
    // but the app still reads/writes from the HashMap.
    // On Day 5, we replace HashMap with JpaRepository.
    // =============================================

    // In-memory storage: maps project ID to Project object (replaced by JpaRepository on Day 5)
    private final Map<Long, Project> projects = new HashMap<>();

    // Simple counter for generating unique IDs (replaced by @GeneratedValue on Day 5)
    private Long nextId = 1L;

    // PATTERN: Constructor injection — Spring provides the bean
    // Pre-load some seed data so we have something to see in the API
    public ProjectService() {
        // Seed project 1
        Project p1 = new Project();
        p1.setId(nextId++);
        p1.setName("IronBoard");
        p1.setDescription("A project management application");
        p1.setCreatedAt(LocalDateTime.now());
        projects.put(p1.getId(), p1);

        // Seed project 2
        Project p2 = new Project();
        p2.setId(nextId++);
        p2.setName("IronLibrary");
        p2.setDescription("A library management system");
        p2.setCreatedAt(LocalDateTime.now());
        projects.put(p2.getId(), p2);
    }

    /**
     * Returns all projects as a list.
     * HashMap.values() equivalent — becomes repository.findAll() on Day 5.
     */
    public List<Project> findAll() {
        return new ArrayList<>(projects.values());
    }

    /**
     * Returns projects whose name contains the given string (case-insensitive).
     */
    public List<Project> findByName(String name) {
        List<Project> result = new ArrayList<>();
        String lowerName = name.toLowerCase();
        for (Project p : projects.values()) {
            if (p.getName().toLowerCase().contains(lowerName)) {
                result.add(p);
            }
        }
        return result;
    }

    // =============================================
    // CHANGED: ResourceNotFoundException instead of RuntimeException
    // =============================================
    // WHY: This is the KEY change for exception handling.
    // Before (Day 6): threw RuntimeException
    //   → Spring returned 500 Internal Server Error with stack trace (wrong!)
    // After (Day 7): throws ResourceNotFoundException
    //   → GlobalExceptionHandler catches it → returns 404 Not Found (correct!)
    //
    // PATTERN: "Throw early" — we check for null immediately after the
    // lookup and throw right away. We don't return null and let the
    // caller deal with it.
    //
    // COMMON MISTAKE: Returning null instead of throwing. If you return null:
    //   - The controller has to check for null (extra code)
    //   - If the controller forgets, the client gets 200 with null body
    //   - No centralized error handling
    // =============================================

    /**
     * Looks up a project by ID.
     * Throws ResourceNotFoundException if the project does not exist.
     */
    public Project findById(Long id) {
        // HashMap.get(id) + null check equivalent — becomes repository.findById(id).orElseThrow() on Day 5
        Project project = projects.get(id);
        if (project == null) {
            throw new ResourceNotFoundException("Project", id);
        }
        return project;
    }

    // =============================================
    // CHANGED from Day 7: create() now receives a Project model
    // object instead of raw parameters. The mapper (ProjectMapper.toModel())
    // converts the request DTO to a model in the controller, then the
    // controller passes the model here.
    //
    // The service sets the INTERNAL fields that the client never provides:
    //   - id (auto-generated)
    //   - createdAt (server timestamp)
    // These fields exist on the model but NOT on the request DTO --
    // that's the whole point of having separate DTOs and models.
    // =============================================

    /**
     * Assigns an ID and timestamp to the project, stores it, and returns it.
     * The caller provides a Project with name and description already set.
     */
    public Project create(Project project) {
        // HashMap.put() equivalent — becomes repository.save() on Day 5
        project.setId(nextId++);
        project.setCreatedAt(LocalDateTime.now());
        projects.put(project.getId(), project);
        return project;
    }

    // =============================================
    // FULL UPDATE -- FOR PUT
    // =============================================
    // CHANGED from Day 7: Receives a Project model object instead
    // of raw parameters. The mapper (ProjectMapper.toModel()) converts
    // the UpdateProjectRequest DTO to a model in the controller.
    //
    // PATTERN: fullUpdate() replaces ALL fields unconditionally.
    // This matches PUT semantics: the client sends the complete
    // resource, and every field is overwritten.
    //
    // TIP: findById() does the existence check for us. If the
    // project doesn't exist, it throws ResourceNotFoundException
    // and we never reach the update logic.
    // =============================================

    /**
     * Replaces all fields of an existing project (PUT semantics).
     * Every field is overwritten, even if the new value is null.
     *
     * @param id the project ID to update
     * @param updates a Project model containing the new field values
     * @return the updated project
     * @throws ResourceNotFoundException if the project doesn't exist
     */
    public Project fullUpdate(Long id, Project updates) {
        Project project = findById(id);
        project.setName(updates.getName());
        project.setDescription(updates.getDescription());
        return project;
    }

    // =============================================
    // PARTIAL UPDATE -- FOR PATCH
    // =============================================
    // CHANGED from Day 7: Same model-based signature as fullUpdate().
    //
    // PATTERN: partialUpdate() only sets fields that are non-null.
    // This matches PATCH semantics.
    //
    // TIP: Compare fullUpdate() vs partialUpdate():
    //   fullUpdate:    project.setName(updates.getName())          -- always sets
    //   partialUpdate: if (updates.getName() != null) { ... }     -- only if sent
    // =============================================

    /**
     * Updates only the non-null fields of an existing project (PATCH semantics).
     * Null fields are left untouched.
     *
     * @param id the project ID to update
     * @param updates a Project model containing the new field values (nulls = don't change)
     * @return the updated project
     * @throws ResourceNotFoundException if the project doesn't exist
     */
    public Project partialUpdate(Long id, Project updates) {
        Project project = findById(id);

        if (updates.getName() != null) {
            project.setName(updates.getName());
        }
        if (updates.getDescription() != null) {
            project.setDescription(updates.getDescription());
        }

        return project;
    }

    // =============================================
    // DELETE METHOD
    // =============================================
    // PATTERN: Verify-then-delete. We call findById() first to ensure
    // the resource exists. If it doesn't, findById() throws
    // ResourceNotFoundException → GlobalExceptionHandler returns 404.
    // =============================================

    /**
     * Deletes a project from the HashMap.
     *
     * @param id the project ID to delete
     * @throws ResourceNotFoundException if the project doesn't exist
     */
    public void delete(Long id) {
        // HashMap.remove() equivalent — becomes repository.deleteById() on Day 5
        findById(id);
        projects.remove(id);
    }
}
