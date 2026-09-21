package com.example.loanrisk;

import com.example.loanrisk.dto.CustomerRiskView;
import com.example.loanrisk.entity.Customer;
import com.example.loanrisk.entity.EmiPayment;
import com.example.loanrisk.service.GeminiService;
import com.example.loanrisk.service.RiskScoringService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RiskScoringServiceTest {

    @Autowired
    private RiskScoringService riskScoringService;

    @Autowired
    private GeminiService geminiService;

    @Test
    public void testAiExplanationIsNeverNull() {
        Customer customer = new Customer("Test User", "9999999999", "test@example.com", "Salaried", 100000.0, 5000.0);
        List<EmiPayment> payments = new ArrayList<>();
        payments.add(new EmiPayment(customer, LocalDate.now().minusMonths(1), LocalDate.now().minusMonths(1)));

        CustomerRiskView view = riskScoringService.scoreCustomer(customer, payments);

        assertNotNull(view.getAiExplanation(), "AI Explanation should never be null");
        assertFalse(view.getAiExplanation().isBlank(), "AI Explanation should not be blank");
        System.out.println("Generated AI Explanation: " + view.getAiExplanation());
    }
}
