package com.example.loanrisk.service;

import com.example.loanrisk.dto.CustomerRiskView;
import com.example.loanrisk.entity.EmiPayment;
import com.example.loanrisk.entity.EmiStatus;
import com.example.loanrisk.entity.RiskLevel;
import com.example.loanrisk.repository.CustomerRepository;
import com.example.loanrisk.repository.EmiPaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Rule-based (explainable) risk scoring engine.
 * Looks at each customer's most recent EMI payments and classifies
 * them as LOW / MEDIUM / HIGH risk, with a plain-English reason.
 *
 * No black-box ML here on purpose — every decision can be traced
 * back to a simple, auditable rule.
 */
@Service
public class RiskScoringService {

    private static final int WINDOW_SIZE = 3; // look at last 3 EMIs

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmiPaymentRepository emiPaymentRepository;

    @Autowired
    private GeminiService geminiService;

    public List<CustomerRiskView> getAllCustomerRiskViews() {
        List<CustomerRiskView> views = new ArrayList<>();
        customerRepository.findAll().forEach(customer -> {
            List<EmiPayment> recent = emiPaymentRepository
                    .findByCustomerIdOrderByDueDateDesc(customer.getId());

            views.add(scoreCustomer(customer, recent));
        });
        return views;
    }

    public CustomerRiskView scoreCustomer(com.example.loanrisk.entity.Customer customer, List<EmiPayment> paymentsDescByDate) {
        int windowCount = Math.min(WINDOW_SIZE, paymentsDescByDate.size());
        List<EmiPayment> window = paymentsDescByDate.subList(0, windowCount);

        long badCount = window.stream()
                .filter(p -> p.getStatus() == EmiStatus.LATE || p.getStatus() == EmiStatus.MISSED)
                .count();

        RiskLevel level;
        String reason;

        if (window.isEmpty()) {
            level = RiskLevel.LOW;
            reason = "No EMI history yet — not enough data, defaulting to Low risk.";
        } else if (badCount >= 2) {
            level = RiskLevel.HIGH;
            reason = badCount + " out of last " + windowCount + " EMIs were late or missed.";
        } else if (badCount == 1) {
            level = RiskLevel.MEDIUM;
            reason = "1 out of last " + windowCount + " EMIs was late or missed.";
        } else {
            level = RiskLevel.LOW;
            reason = "All of the last " + windowCount + " EMIs were paid on time.";
        }

        // Cache-first: only regenerate AI explanation if risk reason changes.
        // Applies to ALL risk levels (LOW, MEDIUM, HIGH).
        String aiExplanation = null;
        String cacheKey = "reason:" + reason; // reason changes when the underlying facts change

        boolean cacheValid = cacheKey.equals(customer.getCachedForRiskLevel())
                && customer.getCachedAiExplanation() != null
                && !customer.getCachedAiExplanation().isBlank();

        if (cacheValid) {
            aiExplanation = customer.getCachedAiExplanation();
        } else {
            aiExplanation = geminiService.generateRiskExplanation(customer.getName(), level.name(), reason);
            if (aiExplanation != null && !aiExplanation.isBlank()) {
                customer.setCachedAiExplanation(aiExplanation);
                customer.setCachedForRiskLevel(cacheKey);
                customerRepository.save(customer);
            }
        }

        if (aiExplanation == null || aiExplanation.isBlank()) {
            aiExplanation = geminiService.generateFallbackExplanation(customer.getName(), level.name(), reason);
        }

        return new CustomerRiskView(customer, level, reason, aiExplanation);
    }
}
