// =============================================
// ProjectControllerTest - Controller unit tests using @WebMvcTest
// =============================================
//
// WHY: Controller tests verify that our REST endpoints:
//   1. Accept the correct HTTP methods and paths (GET /api/projects, POST /api/projects, etc.)
//   2. Return the correct HTTP status codes (200, 201, 204, 400, 404)
//   3. Return the correct JSON response body (field names, values, structure)
//   4. Validate request bodies correctly (reject invalid input with 400)
//
// PATTERN: @WebMvcTest (test slice)
//   - Loads ONLY the web layer: the specified controller, @ControllerAdvice, filters, etc.
//   - Does NOT load services, repositories, or the database -- much faster than @SpringBootTest.
//   - WHY not @SpringBootTest? It loads the ENTIRE application context (all beans, DB connections).
//     That's slow and unnecessary when we only want to test the controller logic.
//
// PATTERN: @MockitoBean (Spring Boot 3.4+)
//   - Creates a Mockito mock AND registers it in the Spring application context.
//   - Replaces the real service bean that the controller depends on.
//   - COMMON MISTAKE: Using @Mock instead of @MockitoBean. @Mock creates a mock but does NOT
//     register it in the Spring context, so the controller won't receive it.
//
// PATTERN: MockMvc
//   - Simulates HTTP requests without starting a real server (no Tomcat, no port binding).
//   - Injected via @Autowired because @WebMvcTest auto-configures it.
//   - TIP: MockMvc is the main tool for testing controllers. It lets you call endpoints
//     and verify the response (status, headers, JSON body) in a single fluent chain.
//
// PATTERN: Arrange / Act / Assert
//   - Arrange: set up test data and configure mock behavior with when().thenReturn()
//   - Act: perform the HTTP request with mockMvc.perform(...)
//   - Assert: verify the response with andExpect(...)
//   - NOTE: In MockMvc tests, Act and Assert are often combined in the same chain.
//
// PATTERN: Test naming -- methodName_scenario_expectedResult (3-part)
//   - You learned this convention in Unit 1 (Testing with JUnit).
//   - Examples: getProjectById_existingId_returnsProject, createProject_invalidBody_returns400
//   - Exception: when there's only one scenario (e.g., getProjects -- always returns a list),
//     the middle part is omitted: getProjects_returnsOk (2-part).
//
// =============================================
package com.ironhack.ironboard.controller;

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

// PATTERN: @WebMvcTest(ControllerClass.class)
// WHY: Specifying the controller class limits what Spring loads. If you omit the parameter,
//      Spring loads ALL controllers, which is slower and can cause unrelated bean errors.
// TIP: Always specify the controller you're testing to keep tests fast and focused.
@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    // PATTERN: MockMvc is auto-configured by @WebMvcTest and injected via @Autowired.
    // WHY: MockMvc simulates HTTP requests (GET, POST, PUT, DELETE) without starting Tomcat.
    //      It processes the request through Spring's DispatcherServlet, so @RequestMapping,
    //      @Valid, @ControllerAdvice, etc. all work exactly like in production.
    // COMMON MISTAKE: Trying to create MockMvc manually. With @WebMvcTest, just @Autowired it.
    @Autowired
    private MockMvc mockMvc;

    // PATTERN: @MockitoBean creates a mock ProjectService and registers it in Spring's context.
    // WHY: The real ProjectService uses a HashMap and has seed data -- we don't want that.
    //      With a mock, we control exactly what the service returns for each test.
    // NOTE: By default, mock methods return null (for objects), empty collections, or 0 (for primitives).
    //       We override this with when().thenReturn() in each test.
    @MockitoBean
    private ProjectService projectService;

    // =============================================
    // PATTERN: Helper method instead of @BeforeEach
    // =============================================
    // WHY: In Unit 1, you learned @BeforeEach for creating shared test state
    //      (e.g., a CreditCard every test operates on). Here we use a helper
    //      method instead because:
    //        - Each test needs DIFFERENT data (different IDs, names, descriptions)
    //        - Some tests don't need test objects at all (validation tests)
    //        - Helper method parameters let us customize each test's data
    //        - @BeforeEach would create objects most tests don't use
    //      @BeforeEach is best when ALL tests start from the SAME state.
    //      Helper methods are best when tests need DIFFERENT variations.
    //
    // TIP: Use fixed dates (LocalDateTime.of(...)) instead of LocalDateTime.now()
    //      so assertions are predictable and not time-dependent.
    // COMMON MISTAKE: Using LocalDateTime.now() in test data, then the createdAt assertion
    //      fails because the formatted string changes every time the test runs.

    private Project createTestProject(Long id, String name, String description) {
        Project project = new Project();
        project.setId(id);
        project.setName(name);
        project.setDescription(description);
        project.setCreatedAt(LocalDateTime.of(2026, 2, 7, 10, 0, 0));
        return project;
    }

    // =============================================
    // Test: GET /api/projects returns 200 OK
    // =============================================
    // PATTERN: Arrange / Act / Assert
    // - Arrange: configure mock to return a list of projects
    // - Act: perform GET request
    // - Assert: verify status 200 and JSON response body

    @Test
    void getProjects_returnsOk() throws Exception {
        // Arrange: tell the mock service what to return
        // WHY: when().thenReturn() configures the mock's behavior for this test.
        //      When the controller calls projectService.findAll(), it gets our test data.
        Project p1 = createTestProject(1L, "IronBoard", "A project management app");
        Project p2 = createTestProject(2L, "IronLibrary", "A library management system");
        when(projectService.findAll()).thenReturn(List.of(p1, p2));

        // Act & Assert: perform GET and verify the response
        // PATTERN: mockMvc.perform(get("/path"))
        //   .andExpect(status().isOk())           -- verify HTTP 200
        //   .andExpect(content().contentType(...)) -- verify response is JSON
        //   .andExpect(jsonPath("$...").value(...)) -- verify JSON fields
        //
        // TIP: jsonPath uses JSONPath syntax:
        //   $           = root of the JSON response
        //   $[0].name   = "name" field of first element in array
        //   $.id        = "id" field of root object (non-array response)
        //
        // TIP: hasSize(2) checks the array length. This is the idiomatic way
        //      in MockMvc tests (vs jsonPath("$.length()").value(2)).
        //
        // NOTE: GET /api/projects returns ProjectSummary (id + name only),
        //       not the full ProjectResponse. So we only assert id and name here.
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("IronBoard"))
                .andExpect(jsonPath("$[1].name").value("IronLibrary"));
    }

    // =============================================
    // Test: GET /api/projects/{id} returns 200 OK
    // =============================================

    @Test
    void getProjectById_existingId_returnsProject() throws Exception {
        // Arrange
        Project project = createTestProject(1L, "IronBoard", "A project management app");
        when(projectService.findById(1L)).thenReturn(project);

        // Act & Assert
        // TIP: For single-object responses, jsonPath starts with "$." (root object)
        //      instead of "$[0]." (first array element).
        // NOTE: GET /api/projects/{id} returns the full ProjectResponse (including createdAt).
        //      Jackson serializes LocalDateTime as ISO-8601 format: "2026-02-07T10:00:00"
        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("IronBoard"))
                .andExpect(jsonPath("$.description").value("A project management app"))
                .andExpect(jsonPath("$.createdAt").value("2026-02-07T10:00:00"));
    }

    // =============================================
    // Test: GET /api/projects/{id} returns 404 when not found
    // =============================================
    // PATTERN: Testing error cases with when().thenThrow()
    // WHY: We need to verify that when the service throws ResourceNotFoundException,
    //      the GlobalExceptionHandler catches it and returns a proper 404 response.

    @Test
    void getProjectById_notFound_returns404() throws Exception {
        // Arrange: tell the mock to throw ResourceNotFoundException
        // NOTE: when().thenThrow() makes the mock throw an exception instead of returning a value.
        //       The controller doesn't catch this -- it bubbles up to the GlobalExceptionHandler.
        // TIP: Use the 2-arg constructor (resourceName, id) to match how the service throws it.
        when(projectService.findById(999L))
                .thenThrow(new ResourceNotFoundException("Project", 999L));

        // Act & Assert
        // WHY: We verify both the HTTP status (404) and the error response body.
        //      This proves that @ControllerAdvice is working correctly in our test.
        // NOTE: @WebMvcTest loads @ControllerAdvice automatically -- no extra config needed.
        mockMvc.perform(get("/api/projects/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Project not found with id: 999"));
    }

    // =============================================
    // Test: POST /api/projects returns 201 Created
    // =============================================
    // PATTERN: Testing POST with a JSON request body
    // WHY: POST requests send data in the request body as JSON. We need to:
    //   1. Set the Content-Type header to application/json
    //   2. Provide the JSON body as a string
    //   3. Verify the response status (201) and body
    //
    // WHY any() instead of exact matching?
    //   In the CFU lesson, exact matching is used: productService.create("Keyboard", 79.99)
    //   because the service takes individual parameters (String, double).
    //   Here, the controller calls ProjectMapper.toModel(request) first, creating a NEW
    //   Project object. We can't predict the exact Project instance the mapper creates,
    //   so we use any(Project.class) to match any Project argument.
    //   This is the same reason the lesson uses any() for PUT/update.

    @Test
    void createProject_validRequest_returnsCreated() throws Exception {
        // Arrange: mock the service to return a new project
        Project created = createTestProject(3L, "New Project", "A brand new project");
        when(projectService.create(any(Project.class))).thenReturn(created);

        // TIP: Text blocks (triple quotes """) are a Java 17+ feature.
        //      They make multi-line JSON strings clean and readable.
        //      No need for string concatenation or escape characters.
        // COMMON MISTAKE: Forgetting to set .contentType(MediaType.APPLICATION_JSON).
        //      Without it, Spring returns 415 Unsupported Media Type.
        String requestBody = """
                {
                    "name": "New Project",
                    "description": "A brand new project"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("New Project"));
    }

    // =============================================
    // Test: POST /api/projects with invalid body returns 400
    // =============================================
    // PATTERN: Testing validation errors
    // WHY: The controller uses @Valid on the request body. When validation fails
    //      (e.g., missing @NotBlank field), Spring throws MethodArgumentNotValidException.
    //      The GlobalExceptionHandler catches this and returns a 400 with field errors.
    // NOTE: We do NOT need to configure the mock for this test because the validation
    //       fails BEFORE the controller method body executes -- the service is never called.

    @Test
    void createProject_invalidBody_returns400() throws Exception {
        // Missing required "name" field -- should trigger @NotBlank validation
        String requestBody = """
                {
                    "description": "No name provided"
                }
                """;

        // Act & Assert: expect 400 Bad Request with validation error details
        // TIP: jsonPath("$.fieldErrors") is now an array of "field: message" strings.
        //      hasItem(...) checks that the array contains the expected error string,
        //      regardless of order. This is robust because field error order is not guaranteed.
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors", hasItem("name: Project name is required")));
    }

    // =============================================
    // Test: PUT /api/projects/{id} returns 200 OK
    // =============================================
    // PATTERN: Testing PUT with eq() + any() argument matchers
    // WHY: The controller calls projectService.fullUpdate(id, project).
    //      We use eq(1L) to match the exact ID and any(Project.class) because
    //      the mapper creates a new Project object we can't predict.
    //
    // TIP: When mixing exact values and matchers in a single mock call, ALL arguments
    //      must use matchers. You can't mix eq(1L) with a raw Project object.
    //      Use eq() for exact values and any() for "don't care" values.

    @Test
    void updateProject_validRequest_returnsUpdatedProject() throws Exception {
        // Arrange
        Project updated = createTestProject(1L, "Updated Name", "Updated description");
        when(projectService.fullUpdate(eq(1L), any(Project.class))).thenReturn(updated);

        String requestBody = """
                {
                    "name": "Updated Name",
                    "description": "Updated description"
                }
                """;

        // Act & Assert
        mockMvc.perform(put("/api/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    // =============================================
    // Test: DELETE /api/projects/{id} returns 204 No Content
    // =============================================
    // PATTERN: Testing void methods with doNothing().when()
    // WHY: The delete() method returns void, so we can't use when().thenReturn().
    //      Instead, we use doNothing().when(service).delete(id) to tell the mock
    //      "when delete(1L) is called, do nothing (no exception)."
    //
    // TIP: For void methods, the syntax is reversed:
    //      doNothing().when(service).method()   -- NOT when(service.method()).thenDoNothing()
    //      doThrow(...).when(service).method()   -- NOT when(service.method()).thenThrow(...)

    @Test
    void deleteProject_existingId_returnsNoContent() throws Exception {
        // Arrange
        doNothing().when(projectService).delete(1L);

        // Act & Assert
        // NOTE: 204 No Content has no response body -- we only check the status.
        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isNoContent());
    }

    // =============================================
    // Test: DELETE /api/projects/{id} returns 404 when not found
    // =============================================
    // PATTERN: Testing void methods that throw with doThrow().when()
    // WHY: When delete() is called with a non-existent ID, the service throws
    //      ResourceNotFoundException. We verify the GlobalExceptionHandler returns 404.

    @Test
    void deleteProject_notFound_returns404() throws Exception {
        // Arrange: mock delete to throw ResourceNotFoundException
        doThrow(new ResourceNotFoundException("Project", 999L))
                .when(projectService).delete(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/projects/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Project not found with id: 999"));
    }
}
