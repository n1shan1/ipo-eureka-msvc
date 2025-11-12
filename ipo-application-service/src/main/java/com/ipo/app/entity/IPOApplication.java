package com.ipo.app.entity;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "ipo_applications", uniqueConstraints = @UniqueConstraint(columnNames = { "ipo_id", "investor_id" }))
public class IPOApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", unique = true)
    private String applicationId;

    @Column(name = "ipo_id")
    private String ipoId;

    @Column(name = "investor_id")
    private String investorId;

    private int lots;

    private String status;

    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    public enum Status {
        PENDING, APPROVED, REJECTED
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getIpoId() {
        return ipoId;
    }

    public void setIpoId(String ipoId) {
        this.ipoId = ipoId;
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

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        IPOApplication that = (IPOApplication) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "IPOApplication{" +
                "id=" + id +
                ", applicationId='" + applicationId + '\'' +
                ", ipoId='" + ipoId + '\'' +
                ", investorId='" + investorId + '\'' +
                ", lots=" + lots +
                ", status=" + status +
                ", idempotencyKey='" + idempotencyKey + '\'' +
                '}';
    }
}