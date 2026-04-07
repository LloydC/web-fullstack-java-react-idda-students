// =============================================
// TaskControllerTest - Controller unit tests using @WebMvcTest
// =============================================
//
// PATTERN: Same testing approach as ProjectControllerTest:
//   - @WebMvcTest loads only the TaskController (web layer slice)
//   - @MockitoBean replaces the real TaskService with a mock
//   - MockMvc simulates HTTP requests without a real server
//   - Arrange/Act/Assert pattern in each test method
//
// WHY: We test each controller independently. TaskControllerTest only loads
//      TaskController, not ProjectController. This keeps tests isolated and fast.
//
// TIP: Notice how the tests mirror the controller endpoints:
//   - GET /api/tasks         -> getTasks_returnsOk()
//   - POST /api/tasks        -> createTask_validRequest_returnsCreated()
//   - POST /api/tasks (bad)  -> createTask_invalidBody_returns400()
//   - PUT /api/tasks/{id}    -> updateTask_validRequest_returnsUpdatedTask()
//   - DELETE /api/tasks/{id} -> deleteTask_existingId_returnsNoContent()
//   - DELETE /api/tasks/{id} -> deleteTask_notFound_returns404()
//   Each test covers one endpoint + one scenario (happy path or error case).
//
// =============================================
package com.ironhack.ironboard.controller;

import com.ironhack.ironboard.exception.ResourceNotFoundException;
import com.ironhack.ironboard.entity.Task;
import com.ironhack.ironboard.entity.TaskStatus;
import com.ironhack.ironboard.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
// IMPORTANT: @MockitoBean (Spring Boot 3.4+) replaces the deprecated @MockBean
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

// PATTERN: @WebMvcTest(TaskController.class) -- only loads the TaskController
@WebMvcTest(TaskController.class)
class TaskControllerTest {

    // PATTERN: MockMvc injected via @Autowired (auto-configured by @WebMvcTest)
    @Autowired
    private MockMvc mockMvc;

    // PATTERN: @MockitoBean creates a mock TaskService in the Spring context
    // WHY: The real TaskService has seed data and HashMap logic we don't want in tests.
    //      The mock gives us full control over what the service returns.
    @MockitoBean
    private TaskService taskService;

    // =============================================
    // PATTERN: Helper method instead of @BeforeEach
    // =============================================
    // WHY: In Unit 1, you learned @BeforeEach for creating shared test state
    //      (e.g., a CreditCard every test operates on). Here we use a helper
    //      method instead because:
    //        - Each test needs DIFFERENT data (different IDs, titles, statuses)
    //        - Some tests don't need test objects at all (validation tests)
    //        - Helper method parameters let us customize each test's data
    //        - @BeforeEach would create objects most tests don't use
    //      @BeforeEach is best when ALL tests start from the SAME state.
    //      Helper methods are best when tests need DIFFERENT variations.
    //
    // NOTE: Unlike Project, Task doesn't have a createdAt field, so no date concerns here.

    private Task createTestTask(Long id, String title, String description,
                                TaskStatus status, String type, Long projectId) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setType(type);
        task.setProjectId(projectId);
        return task;
    }

    // =============================================
    // Test: GET /api/tasks returns 200 OK
    // =============================================

    @Test
    void getTasks_returnsOk() throws Exception {
        // Arrange
        // TIP: Create test data that represents realistic scenarios.
        //      Here we have tasks with different statuses and types to verify
        //      that the mapper converts enums to strings correctly.
        Task t1 = createTestTask(1L, "Set up project", "Create Spring Boot project",
                TaskStatus.DONE, "FEATURE", 1L);
        Task t2 = createTestTask(2L, "Fix search bug", "Search returns wrong results",
                TaskStatus.IN_PROGRESS, "BUG", 2L);
        when(taskService.findAll()).thenReturn(List.of(t1, t2));

        // Act & Assert
        // NOTE: We verify that TaskStatus enum values are serialized as strings
        //       (e.g., "DONE", "IN_PROGRESS") -- this is handled by the TaskMapper.
        // COMMON MISTAKE: Expecting the enum object in JSON instead of its string name.
        //      The mapper calls task.getStatus().name() to convert enum -> string.
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("Set up project"))
                .andExpect(jsonPath("$[0].status").value("DONE"))
                .andExpect(jsonPath("$[0].type").value("FEATURE"))
                .andExpect(jsonPath("$[1].title").value("Fix search bug"))
                .andExpect(jsonPath("$[1].status").value("IN_PROGRESS"));
    }

    // =============================================
    // Test: POST /api/tasks returns 201 Created
    // =============================================
    //
    // WHY any() instead of exact matching?
    //   In the CFU lesson, exact matching is used: productService.create("Keyboard", 79.99)
    //   because the service takes individual parameters (String, double).
    //   Here, the controller calls TaskMapper.toModel(request) first, creating a NEW
    //   Task object. We can't predict the exact Task instance the mapper creates,
    //   so we use any() to match any Task argument.
    //   This is the same reason the lesson uses any() for PUT/update.

    @Test
    void createTask_validRequest_returnsCreated() throws Exception {
        // Arrange
        Task created = createTestTask(4L, "Write tests", "Add MockMVC tests",
                TaskStatus.TODO, "FEATURE", 1L);
        when(taskService.create(any())).thenReturn(created);

        // TIP: Text blocks (""") keep JSON clean and readable.
        //      Each field must match the DTO's field names exactly.
        // COMMON MISTAKE: Using the wrong field names in the JSON body
        //      (e.g., "project_id" instead of "projectId"). Java uses camelCase by default.
        String requestBody = """
                {
                    "title": "Write tests",
                    "description": "Add MockMVC tests",
                    "type": "FEATURE",
                    "projectId": 1
                }
                """;

        // Act & Assert
        // WHY: We verify that:
        //   - Status is 201 (from ResponseEntity.status(HttpStatus.CREATED))
        //   - The response body contains the created task's data
        //   - The status field is "TODO" (default set by service)
        //   - The projectId is correctly included in the response
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.title").value("Write tests"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.projectId").value(1));
    }

    // =============================================
    // Test: POST /api/tasks with invalid body returns 400
    // =============================================
    // PATTERN: Testing multiple validation errors at once
    // WHY: CreateTaskRequest has @NotBlank on "title" and @NotNull on "projectId".
    //      When both are missing, we expect BOTH field errors in the response.
    // NOTE: Validation runs BEFORE the controller method body, so the mock service
    //       is never called. No need for when().thenReturn() in this test.

    @Test
    void createTask_invalidBody_returns400() throws Exception {
        // Missing required "title" and "projectId" -- should trigger validation errors
        String requestBody = """
                {
                    "description": "No title or projectId"
                }
                """;

        // Act & Assert
        // TIP: We can verify multiple field errors in a single test.
        //      jsonPath("$.fieldErrors") is an array of "field: message" strings.
        //      hasItem(...) checks for each expected error, regardless of order.
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors", hasItem("title: Task title is required")))
                .andExpect(jsonPath("$.fieldErrors", hasItem("projectId: Project ID is required")));
    }

    // =============================================
    // Test: PUT /api/tasks/{id} returns 200 OK
    // =============================================
    // PATTERN: Testing PUT with eq() + any() argument matchers
    // WHY: The controller calls taskService.fullUpdate(id, task).
    //      eq(1L) matches the exact ID, any(Task.class) matches the mapper's output.

    @Test
    void updateTask_validRequest_returnsUpdatedTask() throws Exception {
        // Arrange
        Task updated = createTestTask(1L, "Updated title", "Updated description",
                TaskStatus.IN_PROGRESS, "BUG", 1L);
        when(taskService.fullUpdate(eq(1L), any(Task.class))).thenReturn(updated);

        String requestBody = """
                {
                    "title": "Updated title",
                    "description": "Updated description",
                    "type": "BUG"
                }
                """;

        // Act & Assert
        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.type").value("BUG"));
    }

    // =============================================
    // Test: DELETE /api/tasks/{id} returns 204 No Content
    // =============================================
    // PATTERN: Testing void methods with doNothing().when()
    // WHY: delete() returns void, so we use doNothing() to say "don't throw".
    //      The reversed syntax doNothing().when(service).method() is required for void methods.

    @Test
    void deleteTask_existingId_returnsNoContent() throws Exception {
        // Arrange
        doNothing().when(taskService).delete(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());
    }

    // =============================================
    // Test: DELETE /api/tasks/{id} returns 404 when not found
    // =============================================
    // PATTERN: Testing void methods that throw with doThrow().when()

    @Test
    void deleteTask_notFound_returns404() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Task", 999L))
                .when(taskService).delete(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task not found with id: 999"));
    }
}
