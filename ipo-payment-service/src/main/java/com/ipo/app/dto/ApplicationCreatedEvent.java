package com.ipo.app.dto;

public class ApplicationCreatedEvent {
    private String applicationId;
    private String investorId;
    private String userUpiId;
    private double amount;

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