package com.ironhack.ironboard.repository;

import com.ironhack.ironboard.entity.Project;
import com.ironhack.ironboard.entity.Task;
import com.ironhack.ironboard.entity.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private Project testProject;

    @BeforeEach
    void setUp() {
        testProject = new Project();
        testProject.setName("Test Project");
        testProject.setDescription("Project for testing");
        testProject = projectRepository.save(testProject);

        Task t1 = new Task();
        t1.setTitle("Task 1");
        t1.setStatus(TaskStatus.TODO);
        t1.setProject(testProject);

        Task t2 = new Task();
        t2.setTitle("Task 2");
        t2.setStatus(TaskStatus.TODO);
        t2.setProject(testProject);

        Task t3 = new Task();
        t3.setTitle("Task 3");
        t3.setStatus(TaskStatus.IN_PROGRESS);
        t3.setProject(testProject);

        taskRepository.saveAll(List.of(t1, t2, t3));
    }

    @Test
    void findByProjectId_returnsTasksForProject() {
        List<Task> tasks = taskRepository.findByProjectId(testProject.getId());
        assertEquals(3, tasks.size());
    }

    @Test
    void countByStatusForProject_returnsCorrectCounts() {
        List<Object[]> results = taskRepository.countByStatusForProject(testProject.getId());
        assertEquals(2, results.size());
    }
}
