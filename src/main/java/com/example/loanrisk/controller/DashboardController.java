package com.example.loanrisk.controller;

import com.example.loanrisk.entity.Customer;
import com.example.loanrisk.entity.EmiPayment;
import com.example.loanrisk.repository.CustomerRepository;
import com.example.loanrisk.repository.EmiPaymentRepository;
import com.example.loanrisk.service.RiskScoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
public class DashboardController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmiPaymentRepository emiPaymentRepository;

    @Autowired
    private RiskScoringService riskScoringService;

    @Autowired
    private com.example.loanrisk.service.EmailService emailService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // Main dashboard: list of customers with risk scores
    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("riskViews", riskScoringService.getAllCustomerRiskViews());
        return "dashboard";
    }

    // Form to add a new customer
    @GetMapping("/customers/new")
    public String newCustomerForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "add-customer";
    }

    @PostMapping("/customers")
    public String saveCustomer(@ModelAttribute Customer customer) {
        customerRepository.save(customer);
        return "redirect:/";
    }

    // Form to log an EMI payment for a customer
    @GetMapping("/customers/{id}/emi/new")
    public String newEmiForm(@PathVariable Long id, Model model) {
        Customer customer = customerRepository.findById(id).orElseThrow();
        model.addAttribute("customer", customer);
        return "add-emi";
    }

    @PostMapping("/customers/{id}/emi")
    public String saveEmi(@PathVariable Long id,
                           @RequestParam String dueDate,
                           @RequestParam(required = false) String paidDate) {
        Customer customer = customerRepository.findById(id).orElseThrow();

        LocalDate due = LocalDate.parse(dueDate);
        LocalDate paid = (paidDate != null && !paidDate.isBlank()) ? LocalDate.parse(paidDate) : null;

        EmiPayment payment = new EmiPayment(customer, due, paid);
        emiPaymentRepository.save(payment);

        // Auto-send an email alert if this payment came in late or wasn't paid at all
        if (payment.getStatus() == com.example.loanrisk.entity.EmiStatus.LATE
                || payment.getStatus() == com.example.loanrisk.entity.EmiStatus.MISSED) {
            String reason = "This EMI of Rs. " + customer.getEmiAmount()
                    + " due on " + due + " was recorded as " + payment.getStatus() + ".";
            emailService.sendEmiAlert(customer, payment.getStatus(), reason);
        }

        return "redirect:/";
    }
}
