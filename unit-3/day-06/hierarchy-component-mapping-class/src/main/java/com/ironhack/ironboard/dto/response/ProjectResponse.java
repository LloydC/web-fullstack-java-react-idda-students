package com.ironhack.ironboard.dto.response;

import java.time.LocalDateTime;

// =============================================
// PROJECT RESPONSE DTO — Day 8: DTOs & Mappers
// =============================================
// NEW: This is a Response DTO — it defines what the API
// RETURNS to the client.
//
// WHY: We never return the domain model (Project) directly.
// Reasons:
// 1. DECOUPLING: The internal model can change (add fields,
//    rename columns, add JPA annotations) without breaking
//    the API contract for clients.
// 2. SECURITY: You control exactly which fields the client
//    sees. Internal fields (passwords, internal flags) are
//    never accidentally exposed.
// 3. STABILITY: Frontend developers rely on a stable API.
//    If you return models directly, any model change is
//    a breaking change.
//
// PATTERN: Request DTOs vs Response DTOs
// - Request DTOs (CreateProjectRequest, UpdateProjectRequest):
//   Define what the CLIENT SENDS to the server.
//   Have validation annotations (@NotBlank, @Size).
//   Are MUTABLE (no-arg constructor + setters) because Jackson
//   needs to deserialize incoming JSON into them.
// - Response DTOs (ProjectResponse):
//   Define what the SERVER RETURNS to the client.
//   Have NO validation — they are output-only.
//   Are IMMUTABLE (final fields + all-args constructor + getters only)
//   because Jackson only needs to SERIALIZE them (calls getters).
//
// PATTERN: Polymorphic response — one DTO for all project types.
//   Instead of separate DevelopmentProjectResponse and
//   ConsultingProjectResponse, we include ALL fields in ONE DTO
//   and use projectType to tell the client which type it is.
//   Subclass fields are null when they don't apply:
//     - projectType="DEVELOPMENT" → techStack set,
//       clientName and hourlyRate are null
//     - projectType="CONSULTING" → clientName and hourlyRate set,
//       techStack is null
//     - projectType="PROJECT" → all subclass fields are null
//
// WHY one response DTO instead of separate ones?
//   Simplicity. The client checks projectType and knows which
//   fields to display. No need for the client to handle different
//   response shapes from the same /api/projects endpoint.
//
// COMMON MISTAKE: Returning the model directly and hoping
// Jackson will serialize it correctly. This works until you
// add a field you do NOT want exposed (like a password hash
// or an internal status flag). By then, clients already
// depend on the shape of the response.
// =============================================
public class ProjectResponse {

    private final Long id;
    private final String name;
    private final String description;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    // PATTERN: projectType discriminator — tells the client which
    // subclass this entity represents. Values: "PROJECT",
    // "DEVELOPMENT", or "CONSULTING".
    private final String projectType;

    // PATTERN: @Embeddable metadata fields flattened into the response.
    // The client sees category and priority as top-level fields,
    // not nested inside a "metadata" object. This keeps the JSON simple.
    private final String category;
    private final String priority;

    // PATTERN: DevelopmentProject-specific fields.
    // Null for base Projects and ConsultingProjects.
    private final String techStack;

    // PATTERN: ConsultingProject-specific fields.
    // Null for base Projects and DevelopmentProjects.
    private final String clientName;
    private final Double hourlyRate;

    public ProjectResponse(Long id, String name, String description,
                           LocalDateTime createdAt, LocalDateTime updatedAt,
                           String projectType,
                           String category, String priority,
                           String techStack,
                           String clientName, Double hourlyRate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.projectType = projectType;
        this.category = category;
        this.priority = priority;
        this.techStack = techStack;
        this.clientName = clientName;
        this.hourlyRate = hourlyRate;
    }

    // Getters only — response DTOs are immutable

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getProjectType() {
        return projectType;
    }

    public String getCategory() {
        return category;
    }

    public String getPriority() {
        return priority;
    }

    public String getTechStack() {
        return techStack;
    }

    public String getClientName() {
        return clientName;
    }

    public Double getHourlyRate() {
        return hourlyRate;
    }
}
