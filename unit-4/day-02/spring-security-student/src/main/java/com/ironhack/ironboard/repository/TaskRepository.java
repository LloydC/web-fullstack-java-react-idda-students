package com.ironhack.ironboard.repository;

import com.ironhack.ironboard.entity.Task;
import com.ironhack.ironboard.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectId(Long projectId);

    List<Task> findByStatus(TaskStatus status);

    // JPQL: GROUP BY aggregation
    @Query("SELECT t.status, COUNT(t) FROM Task t WHERE t.project.id = :projectId GROUP BY t.status")
    List<Object[]> countByStatusForProject(@Param("projectId") Long projectId);

    @Query("SELECT t.project.name, COUNT(t) FROM Task t GROUP BY t.project.name")
    List<Object[]> countTasksPerProject();

    // Native SQL: combined filter
    @Query(value = "SELECT * FROM tasks WHERE project_id = :projectId AND status = :status", nativeQuery = true)
    List<Task> findByProjectIdAndStatusNative(@Param("projectId") Long projectId, @Param("status") String status);
}
