package com.ironhack.ironboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "development_projects")
@PrimaryKeyJoinColumn(name = "id")
public class DevelopmentProject extends Project {

    @Column(name = "tech_stack", length = 200)
    private String techStack;

    public DevelopmentProject() {
    }



    public String getTechStack() {
        return techStack;
    }

    public void setTechStack(String techStack) {
        this.techStack = techStack;
    }
}
