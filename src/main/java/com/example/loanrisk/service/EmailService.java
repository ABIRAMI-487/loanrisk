package com.example.loanrisk.service;

import com.example.loanrisk.entity.Customer;
import com.example.loanrisk.entity.EmiStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends an automatic email to the customer whenever an EMI payment
 * is logged as LATE or MISSED.
 *
 * Message body:
 *  - Tries Gemini (GeminiService) first, for a natural, personalized message.
 *  - Falls back to a fixed template if Gemini isn't configured or fails.
 *
 * The RISK DECISION itself is never touched here - that stays 100%
 * rule-based in RiskScoringService. Gemini only writes the wording.
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private GeminiService geminiService;

    public void sendEmiAlert(Customer customer, EmiStatus status, String riskReason) {
        if (customer.getEmail() == null || customer.getEmail().isBlank()) {
            System.out.println("Skipping email: no email address on file for " + customer.getName());
            return;
        }

        String subject = (status == EmiStatus.MISSED)
                ? "Missed EMI Payment - Action Required"
                : "Late EMI Payment Noticed";

        String body;
        try {
            body = geminiService.generateEmiReminderMessage(
                    customer.getName(),
                    String.valueOf(customer.getEmiAmount()),
                    riskReason,
                    status.name()
            );
            System.out.println("Using Gemini-generated email body for " + customer.getName());
        } catch (Exception e) {
            System.out.println("Gemini unavailable, using fallback template: " + e.getMessage());
            body = fallbackTemplate(customer, status);
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(customer.getEmail());
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            System.out.println("Email sent to " + customer.getEmail());
        } catch (Exception e) {
            System.out.println("Email sending failed (check mail config): " + e.getMessage());
        }
    }

    private String fallbackTemplate(Customer customer, EmiStatus status) {
        if (status == EmiStatus.MISSED) {
            return "Dear " + customer.getName() + ",\n\n"
                 + "We noticed your recent EMI payment of Rs. " + customer.getEmiAmount()
                 + " has not been received yet. Please make the payment at the earliest to avoid "
                 + "penalties or impact on your credit record.\n\n"
                 + "If you have already paid, please ignore this message.\n\n"
                 + "Regards,\nLoan Servicing Team";
        }
        return "Dear " + customer.getName() + ",\n\n"
             + "Your recent EMI payment of Rs. " + customer.getEmiAmount()
             + " was received after the due date. Kindly ensure future payments are made on time "
             + "to keep your repayment record healthy.\n\n"
             + "Regards,\nLoan Servicing Team";
    }
}
