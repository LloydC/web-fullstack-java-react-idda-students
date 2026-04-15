package com.ironhack.ironboard.controller;

import com.ironhack.ironboard.dto.request.FullUpdateTaskRequest;
import com.ironhack.ironboard.exception.ResourceNotFoundException;
import com.ironhack.ironboard.entity.Project;
import com.ironhack.ironboard.entity.Task;
import com.ironhack.ironboard.entity.TaskStatus;
import com.ironhack.ironboard.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    private Task createTestTask(Long id, String title, String description,
                                TaskStatus status, Long projectId) {
        Project project = new Project();
        project.setId(projectId);
        project.setName("Test Project");

        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setProject(project);
        return task;
    }

    @Test
    void getTasks_returnsOk() throws Exception {
        Task t1 = createTestTask(1L, "Set up project", "Create Spring Boot project",
                TaskStatus.DONE, 1L);
        Task t2 = createTestTask(2L, "Fix search bug", "Search returns wrong results",
                TaskStatus.IN_PROGRESS, 2L);
        when(taskService.findAll()).thenReturn(List.of(t1, t2));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("Set up project"))
                .andExpect(jsonPath("$[0].status").value("DONE"))
                .andExpect(jsonPath("$[0].type").value("TASK"))
                .andExpect(jsonPath("$[0].project.id").value(1))
                .andExpect(jsonPath("$[1].title").value("Fix search bug"))
                .andExpect(jsonPath("$[1].status").value("IN_PROGRESS"));
    }

    @Test
    void createTask_validRequest_returnsCreated() throws Exception {
        Task created = createTestTask(4L, "Write tests", "Add MockMVC tests",
                TaskStatus.TODO, 1L);
        when(taskService.create(any(), any())).thenReturn(created);

        String requestBody = """
                {
                    "title": "Write tests",
                    "description": "Add MockMVC tests",
                    "projectId": 1
                }
                """;

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.title").value("Write tests"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.project.id").value(1));
    }

    @Test
    void createTask_invalidBody_returns400() throws Exception {
        String requestBody = """
                {
                    "description": "No title or projectId"
                }
                """;

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors", hasItem("title: Task title is required")))
                .andExpect(jsonPath("$.fieldErrors", hasItem("projectId: Project ID is required")));
    }

    @Test
    void updateTask_validRequest_returnsUpdatedTask() throws Exception {
        Task updated = createTestTask(1L, "Updated title", "Updated description",
                TaskStatus.IN_PROGRESS, 1L);
        when(taskService.fullUpdate(eq(1L), any(FullUpdateTaskRequest.class))).thenReturn(updated);

        String requestBody = """
                {
                    "title": "Updated title",
                    "description": "Updated description",
                    "status": "IN_PROGRESS"
                }
                """;

        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.type").value("TASK"));
    }

    @Test
    void deleteTask_existingId_returnsNoContent() throws Exception {
        doNothing().when(taskService).delete(1L);

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTask_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Task", 999L))
                .when(taskService).delete(999L);

        mockMvc.perform(delete("/api/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task not found with id: 999"));
    }
}
