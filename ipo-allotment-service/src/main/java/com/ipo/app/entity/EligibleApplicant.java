package com.ipo.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "eligible_applicants")
public class EligibleApplicant {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "application_id")
    private String applicationId;

    @Column(name = "investor_id")
    private String investorId;

    private int lots;

    private String status;

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getInvestorId() {
        return investorId;
    }

    public void setInvestorId(String investorId) {
        this.investorId = investorId;
    }

    public int getLots() {
        return lots;
    }

    public void setLots(int lots) {
        this.lots = lots;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "EligibleApplicant{" +
                "id='" + id + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", investorId='" + investorId + '\'' +
                ", lots=" + lots +
                ", status='" + status + '\'' +
                '}';
    }
}