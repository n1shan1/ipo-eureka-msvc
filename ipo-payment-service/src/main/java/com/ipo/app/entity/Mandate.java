package com.ipo.app.entity;

import java.math.BigDecimal;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mandates")
public class Mandate {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "application_id")
    private String applicationId;

    private BigDecimal amount;

    private String status;

    @Column(name = "bank_reference_id")
    private String bankReferenceId;

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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBankReferenceId() {
        return bankReferenceId;
    }

    public void setBankReferenceId(String bankReferenceId) {
        this.bankReferenceId = bankReferenceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Mandate mandate = (Mandate) o;
        return Objects.equals(id, mandate.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Mandate{" +
                "id='" + id + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", amount=" + amount +
                ", status='" + status + '\'' +
                ", bankReferenceId='" + bankReferenceId + '\'' +
                '}';
    }
}