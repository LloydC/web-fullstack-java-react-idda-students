package com.ironhack.ironboard.entity;

// Task subclass for feature tasks. Uses @Inheritance(SINGLE_TABLE).

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("FEATURE")
public class FeatureTask extends Task {

    @Column(name = "story_points")
    private Integer storyPoints;

    public FeatureTask() {
    }

    @Override
    public TaskType getType() {
        return TaskType.FEATURE;
    }

    @Override
    public Integer getStoryPoints() {
        return storyPoints;
    }

    @Override
    public void setStoryPoints(Integer storyPoints) {
        this.storyPoints = storyPoints;
    }
}
