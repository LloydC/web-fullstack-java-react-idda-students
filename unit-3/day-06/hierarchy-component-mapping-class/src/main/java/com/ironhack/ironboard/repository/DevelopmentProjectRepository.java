package com.ironhack.ironboard.repository;

// =============================================
// DEVELOPMENT PROJECT REPOSITORY — Day 06 (Unit 3): Hierarchy & Component Mapping
// =============================================
// PATTERN: Subclass-specific repository for DevelopmentProject.
//
// WHY have a separate repository for the subclass?
//   ProjectRepository (JpaRepository<Project, Long>) returns ALL
//   project types — base Projects, DevelopmentProjects, and
//   ConsultingProjects. When you only want DevelopmentProjects,
//   you need a repository typed to that specific subclass.
//
//   DevelopmentProjectRepository.findAll() returns ONLY rows
//   from the "development_projects" table (joined with "projects").
//   It will never return a ConsultingProject or a base Project.
//
// =============================================

import com.ironhack.ironboard.entity.DevelopmentProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DevelopmentProjectRepository extends JpaRepository<DevelopmentProject, Long> {

    // Derived query method: finds development projects whose techStack
    // contains the given keyword (case-insensitive).
    List<DevelopmentProject> findByTechStackContainingIgnoreCase(String tech);
}
