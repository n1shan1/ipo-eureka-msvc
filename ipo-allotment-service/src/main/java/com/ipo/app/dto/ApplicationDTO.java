package com.ipo.app.dto;

public class ApplicationDTO {
    private String investorId;
    private int lots;

    public ApplicationDTO() {
    }

    public ApplicationDTO(String investorId, int lots) {
        this.investorId = investorId;
        this.lots = lots;
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
}