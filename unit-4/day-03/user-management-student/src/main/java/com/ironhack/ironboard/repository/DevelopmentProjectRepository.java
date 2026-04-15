package com.ironhack.ironboard.repository;

import com.ironhack.ironboard.entity.DevelopmentProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DevelopmentProjectRepository extends JpaRepository<DevelopmentProject, Long> {

    List<DevelopmentProject> findByTechStackContainingIgnoreCase(String tech);
}
