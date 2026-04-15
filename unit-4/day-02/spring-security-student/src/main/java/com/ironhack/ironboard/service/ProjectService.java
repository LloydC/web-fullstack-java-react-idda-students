package com.ironhack.ironboard.service;

import com.ironhack.ironboard.dto.request.FullUpdateProjectRequest;
import com.ironhack.ironboard.dto.request.PatchUpdateProjectRequest;
import com.ironhack.ironboard.entity.ConsultingProject;
import com.ironhack.ironboard.entity.DevelopmentProject;
import com.ironhack.ironboard.entity.Project;
import com.ironhack.ironboard.entity.ProjectMetadata;
import com.ironhack.ironboard.exception.ResourceNotFoundException;
import com.ironhack.ironboard.repository.ConsultingProjectRepository;
import com.ironhack.ironboard.repository.DevelopmentProjectRepository;
import com.ironhack.ironboard.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final DevelopmentProjectRepository developmentProjectRepository;
    private final ConsultingProjectRepository consultingProjectRepository;

    public ProjectService(ProjectRepository projectRepository,
                          DevelopmentProjectRepository developmentProjectRepository,
                          ConsultingProjectRepository consultingProjectRepository) {
        this.projectRepository = projectRepository;
        this.developmentProjectRepository = developmentProjectRepository;
        this.consultingProjectRepository = consultingProjectRepository;
    }

    /**
     * Returns all projects from the database (all types).
     */
    @Transactional(readOnly = true)
    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    /**
     * Returns projects whose name contains the given keyword (case-insensitive).
     */
    @Transactional(readOnly = true)
    public List<Project> findByName(String name) {
        return projectRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Returns all DevelopmentProject entities.
     */
    @Transactional(readOnly = true)
    public List<DevelopmentProject> findAllDevelopment() {
        return developmentProjectRepository.findAll();
    }

    /**
     * Returns all ConsultingProject entities.
     */
    @Transactional(readOnly = true)
    public List<ConsultingProject> findAllConsulting() {
        return consultingProjectRepository.findAll();
    }

    /**
     * Looks up a project by ID.
     * Throws ResourceNotFoundException if the project does not exist.
     */
    @Transactional(readOnly = true)
    public Project findById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
        return project;
    }

    /**
     * Persists a new base Project to the database.
     */
    @Transactional
    public Project create(Project project) {
        return projectRepository.save(project);
    }

    /**
     * Persists a new DevelopmentProject to the database.
     */
    @Transactional
    public DevelopmentProject createDevelopment(DevelopmentProject project) {
        return developmentProjectRepository.save(project);
    }

    /**
     * Persists a new ConsultingProject to the database.
     */
    @Transactional
    public ConsultingProject createConsulting(ConsultingProject project) {
        return consultingProjectRepository.save(project);
    }

    // Update approach: instanceof on loaded entity.
    // Safe because findById() returns the real subclass, not a lazy proxy.
    // Chosen among several valid alternatives for directness at this course level.
    // Contrast: TaskService uses virtual method dispatch instead.

    /**
     * Replaces all fields of an existing project (PUT semantics).
     *
     * @param id the project ID to update
     * @param request the update DTO containing new field values
     * @return the updated project
     * @throws ResourceNotFoundException if the project doesn't exist
     */
    @Transactional
    public Project fullUpdate(Long id, FullUpdateProjectRequest request) {
        Project project = findById(id);

        project.setName(request.getName());
        project.setDescription(request.getDescription());

        ProjectMetadata metadata = new ProjectMetadata();
        metadata.setCategory(request.getCategory());
        metadata.setPriority(request.getPriority());
        project.setMetadata(metadata);

        if (project instanceof DevelopmentProject dp) {
            dp.setTechStack(request.getTechStack());
        } else if (project instanceof ConsultingProject cp) {
            cp.setClientName(request.getClientName());
            cp.setHourlyRate(request.getHourlyRate());
        }

        return projectRepository.save(project);
    }

    /**
     * Updates only the non-null fields of an existing project (PATCH semantics).
     *
     * @param id the project ID to update
     * @param request the update DTO containing fields to change (nulls = don't change)
     * @return the updated project
     * @throws ResourceNotFoundException if the project doesn't exist
     */
    @Transactional
    public Project partialUpdate(Long id, PatchUpdateProjectRequest request) {
        Project project = findById(id);

        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }

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

    /**
     * Deletes a project from the database.
     *
     * @param id the project ID to delete
     * @throws ResourceNotFoundException if the project doesn't exist
     */
    @Transactional
    public void delete(Long id) {
        findById(id);
        projectRepository.deleteById(id);
    }
}
