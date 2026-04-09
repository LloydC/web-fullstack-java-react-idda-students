package com.ironhack.ironboard.mapper;

import com.ironhack.ironboard.dto.request.CreateConsultingProjectRequest;
import com.ironhack.ironboard.dto.request.CreateDevelopmentProjectRequest;
import com.ironhack.ironboard.dto.request.CreateProjectRequest;
import com.ironhack.ironboard.dto.response.ProjectSummary;
import com.ironhack.ironboard.dto.response.ProjectResponse;
import com.ironhack.ironboard.dto.response.TaskSummary;
import com.ironhack.ironboard.entity.ConsultingProject;
import com.ironhack.ironboard.entity.DevelopmentProject;
import com.ironhack.ironboard.entity.Project;
import com.ironhack.ironboard.entity.ProjectMetadata;

import java.util.ArrayList;
import java.util.List;

public class ProjectMapper {

    // Subclass detection: instanceof approach (directly-loaded entities only).
    // Contrast: TaskMapper uses virtual methods (polymorphic dispatch) instead.

    private ProjectMapper() {
    }

    public static Project toModel(CreateProjectRequest request) {
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());

        ProjectMetadata metadata = new ProjectMetadata();
        metadata.setCategory(request.getCategory());
        metadata.setPriority(request.getPriority());
        project.setMetadata(metadata);

        return project;
    }

    public static DevelopmentProject toModel(CreateDevelopmentProjectRequest request) {
        DevelopmentProject project = new DevelopmentProject();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setTechStack(request.getTechStack());

        ProjectMetadata metadata = new ProjectMetadata();
        metadata.setCategory(request.getCategory());
        metadata.setPriority(request.getPriority());
        project.setMetadata(metadata);

        return project;
    }

    public static ConsultingProject toModel(CreateConsultingProjectRequest request) {
        ConsultingProject project = new ConsultingProject();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setClientName(request.getClientName());
        project.setHourlyRate(request.getHourlyRate());

        ProjectMetadata metadata = new ProjectMetadata();
        metadata.setCategory(request.getCategory());
        metadata.setPriority(request.getPriority());
        project.setMetadata(metadata);

        return project;
    }

    public static ProjectResponse toResponse(Project project) {
        String projectType = "PROJECT";
        String techStack = null;
        String clientName = null;
        Double hourlyRate = null;

        if (project instanceof DevelopmentProject dp) {
            projectType = "DEVELOPMENT";
            techStack = dp.getTechStack();
        } else if (project instanceof ConsultingProject cp) {
            projectType = "CONSULTING";
            clientName = cp.getClientName();
            hourlyRate = cp.getHourlyRate();
        }

        String category = project.getMetadata() != null ? project.getMetadata().getCategory() : null;
        String priority = project.getMetadata() != null ? project.getMetadata().getPriority() : null;

        List<TaskSummary> tasks = TaskMapper.toSummaryList(project.getTasks());

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
                hourlyRate,
                tasks
        );
    }

    public static ProjectSummary toSummary(Project project) {
        String projectType = "PROJECT";
        if (project instanceof DevelopmentProject) {
            projectType = "DEVELOPMENT";
        } else if (project instanceof ConsultingProject) {
            projectType = "CONSULTING";
        }
        return new ProjectSummary(project.getId(), project.getName(), projectType);
    }

    public static List<ProjectSummary> toSummaryList(List<Project> projects) {
        List<ProjectSummary> result = new ArrayList<>();
        for (Project project : projects) {
            result.add(toSummary(project));
        }
        return result;
    }
}
