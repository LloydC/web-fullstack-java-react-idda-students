package com.ironhack.ironboard.repository;

// =============================================
// TASK REPOSITORY -- Step 05: Spring Data Repositories
// =============================================
// NEW: Replaces the HashMap in TaskService.
//
// More derived queries below. Each one teaches a different pattern.
// See ProjectRepository.java for the step-by-step breakdown.
// =============================================

import com.ironhack.ironboard.entity.Task;
import com.ironhack.ironboard.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // PATTERN: Nested property access
    // Task has a "project" field, and Project has an "id" field.
    // Spring follows the camelCase path: Project + Id -> project.id
    // So "findByProjectId" means: WHERE project_id = ?
    List<Task> findByProjectId(Long projectId);

    // PATTERN: Exact match on an enum field
    // No condition keyword needed — "By + Property" alone means equals.
    List<Task> findByStatus(TaskStatus status);

    // PATTERN: Stacking conditions on a single property
    // Conditions chain left to right on the same field:
    //   Title + Containing  -> LIKE '%...%'
    //          + IgnoreCase -> wraps in UPPER()
    // You can combine multiple fields with And/Or:
    //   e.g., findByStatusAndTitleContainingIgnoreCase(status, keyword)
    List<Task> findByTitleContainingIgnoreCase(String keyword);
}
