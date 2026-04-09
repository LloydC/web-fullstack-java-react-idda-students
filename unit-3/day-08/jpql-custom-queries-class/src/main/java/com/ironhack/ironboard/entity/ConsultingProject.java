package com.ironhack.ironboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "consulting_projects")
@PrimaryKeyJoinColumn(name = "id")
public class ConsultingProject extends Project {

    @Column(name = "client_name", length = 100)
    private String clientName;

    @Column(name = "hourly_rate")
    private Double hourlyRate;

    public ConsultingProject() {
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(Double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }
}
