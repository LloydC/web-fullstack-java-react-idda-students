package com.ironhack.ironboard.repository;

// =============================================
// CONSULTING PROJECT REPOSITORY — Day 06 (Unit 3): Hierarchy & Component Mapping
// =============================================
// PATTERN: Subclass-specific repository for ConsultingProject.
//
// Same rationale as DevelopmentProjectRepository:
//   - ConsultingProjectRepository.findAll() returns ONLY
//     ConsultingProject entities (joined from consulting_projects
//     + projects tables).
//   - Derived query methods can target ConsultingProject-specific
//     fields like clientName that don't exist on base Project.
//
// =============================================

import com.ironhack.ironboard.entity.ConsultingProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultingProjectRepository extends JpaRepository<ConsultingProject, Long> {

    // Derived query method: finds consulting projects whose clientName
    // contains the given keyword (case-insensitive).
    List<ConsultingProject> findByClientNameContainingIgnoreCase(String clientName);
}
