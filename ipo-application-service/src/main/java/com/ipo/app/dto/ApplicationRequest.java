package com.ipo.app.dto;

public class ApplicationRequest {
    private String investorId;
    private int lots;
    private String userUpiId;

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

    public String getUserUpiId() {
        return userUpiId;
    }

    public void setUserUpiId(String userUpiId) {
        this.userUpiId = userUpiId;
    }
}