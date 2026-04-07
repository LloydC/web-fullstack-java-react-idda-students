package com.ironhack.ironboard.repository;

// =============================================
// TASK REPOSITORY -- Step 05: Spring Data Repositories
// =============================================

import com.ironhack.ironboard.entity.Task;
import com.ironhack.ironboard.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Derived query: finds all tasks for a specific project.
    // SELECT * FROM tasks WHERE project_id = ?
    List<Task> findByProjectId(Long projectId);

    // Derived query: finds all tasks with a specific status.
    // SELECT * FROM tasks WHERE status = ?
    List<Task> findByStatus(TaskStatus status);

    // Derived query: case-insensitive title search.
    // SELECT * FROM tasks WHERE LOWER(title) LIKE LOWER('%keyword%')
    List<Task> findByTitleContainingIgnoreCase(String keyword);
}
