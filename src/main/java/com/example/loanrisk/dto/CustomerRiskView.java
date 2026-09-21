package com.example.loanrisk.dto;

import com.example.loanrisk.entity.Customer;
import com.example.loanrisk.entity.RiskLevel;

public class CustomerRiskView {

    private final Customer customer;
    private final RiskLevel riskLevel;
    private final String reason;
    private final String aiExplanation; // may be null if Gemini isn't configured/available

    public CustomerRiskView(Customer customer, RiskLevel riskLevel, String reason, String aiExplanation) {
        this.customer = customer;
        this.riskLevel = riskLevel;
        this.reason = reason;
        this.aiExplanation = aiExplanation;
    }

    public Customer getCustomer() { return customer; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public String getReason() { return reason; }
    public String getAiExplanation() { return aiExplanation; }
}
