package com.example.loanrisk.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "emi_payments")
public class EmiPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private LocalDate dueDate;
    private LocalDate paidDate;

    @Enumerated(EnumType.STRING)
    private EmiStatus status;

    public EmiPayment() {}

    public EmiPayment(Customer customer, LocalDate dueDate, LocalDate paidDate) {
        this.customer = customer;
        this.dueDate = dueDate;
        this.paidDate = paidDate;
        this.status = computeStatus(dueDate, paidDate);
    }

    // Auto-calculate status: not paid yet -> MISSED, paid >3 days late -> LATE, else ON_TIME
    public static EmiStatus computeStatus(LocalDate dueDate, LocalDate paidDate) {
        if (paidDate == null) {
            return EmiStatus.MISSED;
        }
        if (paidDate.isAfter(dueDate)) {
            return EmiStatus.LATE;
        }
        return EmiStatus.ON_TIME;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getPaidDate() { return paidDate; }
    public void setPaidDate(LocalDate paidDate) { this.paidDate = paidDate; }

    public EmiStatus getStatus() { return status; }
    public void setStatus(EmiStatus status) { this.status = status; }
}
