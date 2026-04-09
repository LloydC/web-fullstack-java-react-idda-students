package com.ironhack.ironboard.repository;

import com.ironhack.ironboard.entity.ConsultingProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultingProjectRepository extends JpaRepository<ConsultingProject, Long> {

    List<ConsultingProject> findByClientNameContainingIgnoreCase(String clientName);
}
