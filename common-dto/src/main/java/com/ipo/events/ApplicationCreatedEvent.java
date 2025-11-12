package com.ipo.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationCreatedEvent implements Serializable {
    private String applicationId;
    private String investorId;
    private String userUpiId;
    private double amount;

    // Manual getters and setters for Lombok compatibility
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

    public String getUserUpiId() {
        return userUpiId;
    }

    public void setUserUpiId(String userUpiId) {
        this.userUpiId = userUpiId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}