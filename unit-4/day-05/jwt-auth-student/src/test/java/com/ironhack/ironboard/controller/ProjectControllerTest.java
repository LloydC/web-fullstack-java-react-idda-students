package com.ironhack.ironboard.controller;

import com.ironhack.ironboard.dto.request.FullUpdateProjectRequest;
import com.ironhack.ironboard.exception.ResourceNotFoundException;
import com.ironhack.ironboard.entity.Project;
import com.ironhack.ironboard.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
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

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    private Project createTestProject(Long id, String name, String description) {
        Project project = new Project();
        project.setId(id);
        project.setName(name);
        project.setDescription(description);
        project.setCreatedAt(LocalDateTime.of(2026, 2, 7, 10, 0, 0));
        return project;
    }

    @Test
    void getProjects_returnsOk() throws Exception {
        Project p1 = createTestProject(1L, "IronBoard", "A project management app");
        Project p2 = createTestProject(2L, "IronLibrary", "A library management system");
        when(projectService.findAll()).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("IronBoard"))
                .andExpect(jsonPath("$[1].name").value("IronLibrary"));
    }

    @Test
    void getProjectById_existingId_returnsProject() throws Exception {
        Project project = createTestProject(1L, "IronBoard", "A project management app");
        when(projectService.findById(1L)).thenReturn(project);

        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("IronBoard"))
                .andExpect(jsonPath("$.description").value("A project management app"))
                .andExpect(jsonPath("$.createdAt").value("2026-02-07T10:00:00"));
    }

    @Test
    void getProjectById_notFound_returns404() throws Exception {
        when(projectService.findById(999L))
                .thenThrow(new ResourceNotFoundException("Project", 999L));

        mockMvc.perform(get("/api/projects/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Project not found with id: 999"));
    }

    @Test
    void createProject_validRequest_returnsCreated() throws Exception {
        Project created = createTestProject(3L, "New Project", "A brand new project");
        when(projectService.create(any(Project.class), any(String.class))).thenReturn(created);

        String requestBody = """
                {
                    "name": "New Project",
                    "description": "A brand new project"
                }
                """;

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("New Project"));
    }

    @Test
    void createProject_invalidBody_returns400() throws Exception {
        String requestBody = """
                {
                    "description": "No name provided"
                }
                """;

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors", hasItem("name: Project name is required")));
    }

    @Test
    void updateProject_validRequest_returnsUpdatedProject() throws Exception {
        Project updated = createTestProject(1L, "Updated Name", "Updated description");
        when(projectService.fullUpdate(eq(1L), any(FullUpdateProjectRequest.class))).thenReturn(updated);

        String requestBody = """
                {
                    "name": "Updated Name",
                    "description": "Updated description"
                }
                """;

        mockMvc.perform(put("/api/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void deleteProject_existingId_returnsNoContent() throws Exception {
        doNothing().when(projectService).delete(1L);

        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProject_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Project", 999L))
                .when(projectService).delete(999L);

        mockMvc.perform(delete("/api/projects/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Project not found with id: 999"));
    }
}
