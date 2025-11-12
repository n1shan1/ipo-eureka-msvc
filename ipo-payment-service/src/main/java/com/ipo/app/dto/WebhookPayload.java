package com.ipo.app.dto;

public class WebhookPayload {
    private String applicationId;
    private String status;
    private String bankReferenceId;
    private String signature;

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
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

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}