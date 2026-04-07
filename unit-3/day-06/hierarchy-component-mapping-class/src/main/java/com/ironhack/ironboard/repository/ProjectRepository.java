package com.ironhack.ironboard.repository;

// =============================================
// PROJECT REPOSITORY -- Step 05: Spring Data Repositories
// =============================================

import com.ironhack.ironboard.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Derived query method: case-insensitive name search.
    // Spring parses the method name and generates:
    //   SELECT * FROM projects WHERE LOWER(name) LIKE LOWER('%keyword%')
    List<Project> findByNameContainingIgnoreCase(String keyword);
}
