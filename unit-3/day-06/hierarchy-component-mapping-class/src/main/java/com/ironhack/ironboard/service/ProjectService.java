package com.ironhack.ironboard.service;

import com.ironhack.ironboard.dto.request.UpdateProjectRequest;
import com.ironhack.ironboard.entity.ConsultingProject;
import com.ironhack.ironboard.entity.DevelopmentProject;
import com.ironhack.ironboard.entity.Project;
import com.ironhack.ironboard.entity.ProjectMetadata;
import com.ironhack.ironboard.exception.ResourceNotFoundException;
import com.ironhack.ironboard.repository.ConsultingProjectRepository;
import com.ironhack.ironboard.repository.DevelopmentProjectRepository;
import com.ironhack.ironboard.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// =============================================
// PROJECT SERVICE — Day 06 (Unit 3): Hierarchy & Component Mapping
// =============================================
// CHANGED from previous step:
//   1. Injected DevelopmentProjectRepository and ConsultingProjectRepository
//      for subclass-specific CRUD operations.
//   2. Added createDevelopment() and createConsulting() methods.
//   3. Added findAllDevelopment() and findAllConsulting() methods.
//   4. fullUpdate() and partialUpdate() now accept UpdateProjectRequest
//      directly (not a mapper-created Project) so the service can
//      apply subclass-specific fields using instanceof checks.
// =============================================
@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final DevelopmentProjectRepository developmentProjectRepository;
    private final ConsultingProjectRepository consultingProjectRepository;

    // PATTERN: Constructor injection with multiple repositories.
    public ProjectService(ProjectRepository projectRepository,
                          DevelopmentProjectRepository developmentProjectRepository,
                          ConsultingProjectRepository consultingProjectRepository) {
        this.projectRepository = projectRepository;
        this.developmentProjectRepository = developmentProjectRepository;
        this.consultingProjectRepository = consultingProjectRepository;
    }

    /**
     * Returns all projects from the database (all types).
     * Uses the base ProjectRepository, which returns Project,
     * DevelopmentProject, and ConsultingProject entities.
     */
    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    /**
     * Returns projects whose name contains the given keyword (case-insensitive).
     */
    public List<Project> findByName(String name) {
        return projectRepository.findByNameContainingIgnoreCase(name);
    }

    // =============================================
    // SUBCLASS-SPECIFIC FIND METHODS
    // =============================================
    // PATTERN: Use subclass repositories to query ONLY that type.
    // =============================================

    /**
     * Returns all DevelopmentProject entities.
     */
    public List<DevelopmentProject> findAllDevelopment() {
        return developmentProjectRepository.findAll();
    }

    /**
     * Returns all ConsultingProject entities.
     */
    public List<ConsultingProject> findAllConsulting() {
        return consultingProjectRepository.findAll();
    }

    /**
     * Looks up a project by ID.
     */
    public Project findById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }

    // =============================================
    // CREATE — Base Project, DevelopmentProject, ConsultingProject
    // =============================================
    // PATTERN: Separate create methods for each entity type.
    // =============================================

    /**
     * Persists a new base Project to the database.
     */
    public Project create(Project project) {
        return projectRepository.save(project);
    }

    /**
     * Persists a new DevelopmentProject to the database.
     * Inserts into both "projects" and "development_projects" tables.
     */
    public DevelopmentProject createDevelopment(DevelopmentProject project) {
        return developmentProjectRepository.save(project);
    }

    /**
     * Persists a new ConsultingProject to the database.
     * Inserts into both "projects" and "consulting_projects" tables.
     */
    public ConsultingProject createConsulting(ConsultingProject project) {
        return consultingProjectRepository.save(project);
    }

    // =============================================
    // FULL UPDATE -- PUT semantics
    // =============================================
    // The service accepts the DTO directly (not a mapper-created
    // model) because it needs to check instanceof on the LOADED entity
    // to apply subclass-specific fields.
    //
    // FLOW:
    //   1. Load the entity by ID (this is the REAL type — DevelopmentProject, etc.)
    //   2. Apply base fields (name, description) unconditionally
    //   3. Apply embedded metadata (category, priority)
    //   4. Check instanceof on the LOADED entity to apply subclass fields
    //   5. Save — Hibernate updates the correct tables
    // =============================================

    /**
     * Replaces all fields of an existing project (PUT semantics).
     */
    public Project fullUpdate(Long id, UpdateProjectRequest request) {
        Project project = findById(id);

        // Apply base fields unconditionally (PUT replaces everything)
        project.setName(request.getName());
        project.setDescription(request.getDescription());

        // Create embedded metadata from flat DTO fields
        ProjectMetadata metadata = new ProjectMetadata();
        metadata.setCategory(request.getCategory());
        metadata.setPriority(request.getPriority());
        project.setMetadata(metadata);

        // instanceof check on the LOADED entity to apply subclass fields.
        // The request DTO has ALL fields, but we only apply the ones
        // that match the entity's actual type.
        if (project instanceof DevelopmentProject dp) {
            dp.setTechStack(request.getTechStack());
        } else if (project instanceof ConsultingProject cp) {
            cp.setClientName(request.getClientName());
            cp.setHourlyRate(request.getHourlyRate());
        }

        return projectRepository.save(project);
    }

    // =============================================
    // PARTIAL UPDATE -- PATCH semantics
    // =============================================

    /**
     * Updates only the non-null fields of an existing project (PATCH semantics).
     */
    public Project partialUpdate(Long id, UpdateProjectRequest request) {
        Project project = findById(id);

        // Apply base fields only if provided (non-null)
        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }

        // Partial update of embedded metadata.
        // If the entity doesn't have metadata yet, create a new one.
        // Only update the fields that are provided in the request.
        if (request.getCategory() != null || request.getPriority() != null) {
            ProjectMetadata metadata = project.getMetadata() != null
                    ? project.getMetadata()
                    : new ProjectMetadata();
            if (request.getCategory() != null) {
                metadata.setCategory(request.getCategory());
            }
            if (request.getPriority() != null) {
                metadata.setPriority(request.getPriority());
            }
            project.setMetadata(metadata);
        }

        // instanceof check for subclass-specific partial updates.
        // Same as fullUpdate, but with null checks on each field.
        if (project instanceof DevelopmentProject dp) {
            if (request.getTechStack() != null) {
                dp.setTechStack(request.getTechStack());
            }
        } else if (project instanceof ConsultingProject cp) {
            if (request.getClientName() != null) {
                cp.setClientName(request.getClientName());
            }
            if (request.getHourlyRate() != null) {
                cp.setHourlyRate(request.getHourlyRate());
            }
        }

        return projectRepository.save(project);
    }

    // =============================================
    // DELETE
    // =============================================
    /**
     * Deletes a project from the database.
     */
    public void delete(Long id) {
        findById(id);
        projectRepository.deleteById(id);
    }
}
