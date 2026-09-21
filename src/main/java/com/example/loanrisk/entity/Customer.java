package com.example.loanrisk.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phone;
    private String email;
    private String jobType;
    private Double loanAmount;
    private Double emiAmount;

    // Cache for the last AI-generated explanation, so we don't call Gemini
    // on every single dashboard refresh (avoids rate limits + is faster).
    // Only regenerated when the risk level actually changes.
    @Column(length = 1000)
    private String cachedAiExplanation;

    private String cachedForRiskLevel;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmiPayment> payments = new ArrayList<>();

    public Customer() {}

    public Customer(String name, String phone, String email, String jobType, Double loanAmount, Double emiAmount) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.jobType = jobType;
        this.loanAmount = loanAmount;
        this.emiAmount = emiAmount;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }

    public Double getLoanAmount() { return loanAmount; }
    public void setLoanAmount(Double loanAmount) { this.loanAmount = loanAmount; }

    public Double getEmiAmount() { return emiAmount; }
    public void setEmiAmount(Double emiAmount) { this.emiAmount = emiAmount; }

    public String getCachedAiExplanation() { return cachedAiExplanation; }
    public void setCachedAiExplanation(String cachedAiExplanation) { this.cachedAiExplanation = cachedAiExplanation; }

    public String getCachedForRiskLevel() { return cachedForRiskLevel; }
    public void setCachedForRiskLevel(String cachedForRiskLevel) { this.cachedForRiskLevel = cachedForRiskLevel; }

    public List<EmiPayment> getPayments() { return payments; }
    public void setPayments(List<EmiPayment> payments) { this.payments = payments; }
}
