package com.ironhack.ironboard.mapper;

import com.ironhack.ironboard.dto.request.CreateTaskRequest;
import com.ironhack.ironboard.dto.response.ProjectSummary;
import com.ironhack.ironboard.dto.response.TaskResponse;
import com.ironhack.ironboard.dto.response.TaskSummary;
import com.ironhack.ironboard.entity.BugTask;
import com.ironhack.ironboard.entity.FeatureTask;
import com.ironhack.ironboard.entity.Task;
import com.ironhack.ironboard.entity.TaskType;

import java.util.ArrayList;
import java.util.List;

public class TaskMapper {

    // Subclass detection: virtual methods (polymorphic dispatch).
    // Safest approach
    // Contrast: ProjectMapper uses instanceof instead.

    private TaskMapper() {
    }

    public static Task toModel(CreateTaskRequest request) {
        Task task;

        if (request.getType() == TaskType.FEATURE) {
            FeatureTask ft = new FeatureTask();
            ft.setStoryPoints(request.getStoryPoints());
            task = ft;
        } else if (request.getType() == TaskType.BUG) {
            BugTask bt = new BugTask();
            bt.setSeverity(request.getSeverity());
            task = bt;
        } else {
            task = new Task();
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        return task;
    }

    public static TaskResponse toResponse(Task task) {
        String type = task.getType().name();
        Integer storyPoints = task.getStoryPoints();
        String severity = task.getSeverity();

        ProjectSummary project = ProjectMapper.toSummary(task.getProject());

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus().name(),
                type,
                project,
                storyPoints,
                severity
        );
    }

    public static List<TaskResponse> toResponseList(List<Task> tasks) {
        List<TaskResponse> result = new ArrayList<>();
        for (Task task : tasks) {
            result.add(toResponse(task));
        }
        return result;
    }

    /**
     * Converts a Task to a lightweight TaskSummary for embedding inside ProjectResponse.
     */
    public static TaskSummary toSummary(Task task) {
        String type = task.getType().name();
        return new TaskSummary(
                task.getId(),
                task.getTitle(),
                task.getStatus().name(),
                type
        );
    }

    /**
     * Converts a list of Tasks to TaskSummary DTOs.
     */
    public static List<TaskSummary> toSummaryList(List<Task> tasks) {
        List<TaskSummary> result = new ArrayList<>();
        for (Task task : tasks) {
            result.add(toSummary(task));
        }
        return result;
    }
}
