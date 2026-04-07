package com.ironhack.ironboard.mapper;

import com.ironhack.ironboard.dto.request.CreateConsultingProjectRequest;
import com.ironhack.ironboard.dto.request.CreateDevelopmentProjectRequest;
import com.ironhack.ironboard.dto.request.CreateProjectRequest;
import com.ironhack.ironboard.dto.response.ProjectSummary;
import com.ironhack.ironboard.dto.response.ProjectResponse;
import com.ironhack.ironboard.entity.ConsultingProject;
import com.ironhack.ironboard.entity.DevelopmentProject;
import com.ironhack.ironboard.entity.Project;
import com.ironhack.ironboard.entity.ProjectMetadata;

import java.util.ArrayList;
import java.util.List;

// =============================================
// PROJECT MAPPER -- Day 8: DTOs & Mappers
// =============================================

public class ProjectMapper {

    // Private constructor prevents instantiation -- all methods are static
    private ProjectMapper() {
    }

    // =============================================
    // toModel(CreateProjectRequest) -- Inbound conversion for POST /api/projects
    // =============================================
    public static Project toModel(CreateProjectRequest request) {
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());

        // Create embedded metadata from flat DTO fields.
        ProjectMetadata metadata = new ProjectMetadata();
        metadata.setCategory(request.getCategory());
        metadata.setPriority(request.getPriority());
        project.setMetadata(metadata);

        return project;
    }

    // =============================================
    // toModel(CreateDevelopmentProjectRequest) -- Inbound conversion
    // for POST /api/projects/development
    //
    // Creates a DevelopmentProject (subclass) with both
    // the base fields (name, description, metadata) and the
    // subclass-specific fields (techStack).
    // =============================================
    public static DevelopmentProject toModel(CreateDevelopmentProjectRequest request) {
        DevelopmentProject project = new DevelopmentProject();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setTechStack(request.getTechStack());

        // Create embedded metadata from flat DTO fields
        ProjectMetadata metadata = new ProjectMetadata();
        metadata.setCategory(request.getCategory());
        metadata.setPriority(request.getPriority());
        project.setMetadata(metadata);

        return project;
    }

    // =============================================
    // toModel(CreateConsultingProjectRequest) -- Inbound conversion
    // for POST /api/projects/consulting
    //
    // Same as above, but for ConsultingProject subclass.
    // Creates the entity with clientName and hourlyRate fields
    // that map to the "consulting_projects" child table.
    // =============================================
    public static ConsultingProject toModel(CreateConsultingProjectRequest request) {
        ConsultingProject project = new ConsultingProject();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setClientName(request.getClientName());
        project.setHourlyRate(request.getHourlyRate());

        // Create embedded metadata from flat DTO fields
        ProjectMetadata metadata = new ProjectMetadata();
        metadata.setCategory(request.getCategory());
        metadata.setPriority(request.getPriority());
        project.setMetadata(metadata);

        return project;
    }

    // =============================================
    // toResponse() -- Converts a Project model to a ProjectResponse DTO
    //
    // PATTERN: instanceof with pattern variable (Java 16+) to check
    // the runtime type of the entity and extract subclass-specific fields.
    //
    //  
    // projectType discriminator — the response includes a
    // string ("PROJECT", "DEVELOPMENT", "CONSULTING") so the client
    // knows which type this is and which fields to expect.
    // =============================================
    public static ProjectResponse toResponse(Project project) {
        // Start with base values, then override for subclass types
        String projectType = "PROJECT";
        String techStack = null;
        String clientName = null;
        Double hourlyRate = null;

        // instanceof with pattern variable (e.g., "DevelopmentProject dp")
        // combines the type check AND the cast in one step.
        if (project instanceof DevelopmentProject dp) {
            projectType = "DEVELOPMENT";
            techStack = dp.getTechStack();
        } else if (project instanceof ConsultingProject cp) {
            projectType = "CONSULTING";
            clientName = cp.getClientName();
            hourlyRate = cp.getHourlyRate();
        }

        // Safely extract embedded metadata fields.
        // metadata can be null if the project was created without it.
        String category = project.getMetadata() != null ? project.getMetadata().getCategory() : null;
        String priority = project.getMetadata() != null ? project.getMetadata().getPriority() : null;

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                projectType,
                category,
                priority,
                techStack,
                clientName,
                hourlyRate
        );
    }

    // =============================================
    // toSummary() -- Converts a Project model to a ProjectSummary DTO
    // =============================================
    public static ProjectSummary toSummary(Project project) {
        String projectType = "PROJECT";
        if (project instanceof DevelopmentProject) {
            projectType = "DEVELOPMENT";
        } else if (project instanceof ConsultingProject) {
            projectType = "CONSULTING";
        }
        return new ProjectSummary(project.getId(), project.getName(), projectType);
    }

    // =============================================
    // toSummaryList() -- Converts a list of Project models to ProjectSummary DTOs
    // =============================================
    public static List<ProjectSummary> toSummaryList(List<Project> projects) {
        List<ProjectSummary> result = new ArrayList<>();
        for (Project project : projects) {
            result.add(toSummary(project));
        }
        return result;
    }
}
