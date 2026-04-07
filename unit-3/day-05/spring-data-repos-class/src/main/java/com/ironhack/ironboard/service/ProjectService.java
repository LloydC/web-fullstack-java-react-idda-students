package com.ironhack.ironboard.service;

import com.ironhack.ironboard.entity.Project;
import com.ironhack.ironboard.exception.ResourceNotFoundException;
import com.ironhack.ironboard.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// =============================================
// PROJECT SERVICE — Step 05: Spring Data Repositories
// =============================================
// CHANGED from Step 04: HashMap → ProjectRepository.
//
// What changed:
//   BEFORE (HashMap):                    AFTER (Repository):
//   ─────────────────                    ──────────────────
//   Map<Long, Project> projects          ProjectRepository projectRepository
//   new ArrayList<>(projects.values())   projectRepository.findAll()
//   projects.get(id) + null check        projectRepository.findById(id).orElseThrow(...)
//   projects.put(id, project)            projectRepository.save(project)
//   projects.remove(id)                  projectRepository.deleteById(id)
//   nextId++ for IDs                     @GeneratedValue handles it
//   LocalDateTime.now() for createdAt    @Column + @PrePersist or DB default
//
// =============================================
@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    // Constructor injection -- Spring provides the ProjectRepository bean
    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    /**
     * Returns all projects from the database.
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

    /**
     * Looks up a project by ID..
     */
    public Project findById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }

    /**
     * Persists a new project to the database.
     * The database generates the ID automatically.
     */
    public Project create(Project project) {
        return projectRepository.save(project);
    }

    /**
     * Replaces all fields of an existing project (PUT semantics).
     */
    public Project fullUpdate(Long id, Project updates) {
        Project project = findById(id);
        project.setName(updates.getName());
        project.setDescription(updates.getDescription());
        return projectRepository.save(project);
    }


    /**
     * Updates only the non-null fields of an existing project (PATCH semantics).
     */
    public Project partialUpdate(Long id, Project updates) {
        Project project = findById(id);

        if (updates.getName() != null) {
            project.setName(updates.getName());
        }
        if (updates.getDescription() != null) {
            project.setDescription(updates.getDescription());
        }

        return projectRepository.save(project);
    }

    /**
     * Deletes a project from the database.
     */
    public void delete(Long id) {
        findById(id);
        projectRepository.deleteById(id);
    }
}
