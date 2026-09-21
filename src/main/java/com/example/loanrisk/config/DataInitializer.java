package com.example.loanrisk.config;

import com.example.loanrisk.entity.Customer;
import com.example.loanrisk.entity.EmiPayment;
import com.example.loanrisk.repository.CustomerRepository;
import com.example.loanrisk.repository.EmiPaymentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final EmiPaymentRepository emiPaymentRepository;

    public DataInitializer(CustomerRepository customerRepository, EmiPaymentRepository emiPaymentRepository) {
        this.customerRepository = customerRepository;
        this.emiPaymentRepository = emiPaymentRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (customerRepository.count() > 0) {
            return;
        }

        // Customer 1: Low Risk (All payments on time)
        Customer c1 = new Customer("Ramesh Kumar", "9876543210", "ramesh@example.com", "Salaried", 500000.0, 15000.0);
        customerRepository.save(c1);
        emiPaymentRepository.save(new EmiPayment(c1, LocalDate.now().minusMonths(3), LocalDate.now().minusMonths(3)));
        emiPaymentRepository.save(new EmiPayment(c1, LocalDate.now().minusMonths(2), LocalDate.now().minusMonths(2)));
        emiPaymentRepository.save(new EmiPayment(c1, LocalDate.now().minusMonths(1), LocalDate.now().minusMonths(1)));

        // Customer 2: Medium Risk (1 Late payment)
        Customer c2 = new Customer("Priya Sharma", "9812345678", "priya@example.com", "Self-Employed", 300000.0, 10000.0);
        customerRepository.save(c2);
        emiPaymentRepository.save(new EmiPayment(c2, LocalDate.now().minusMonths(3), LocalDate.now().minusMonths(3)));
        emiPaymentRepository.save(new EmiPayment(c2, LocalDate.now().minusMonths(2), LocalDate.now().minusMonths(2).plusDays(5)));
        emiPaymentRepository.save(new EmiPayment(c2, LocalDate.now().minusMonths(1), LocalDate.now().minusMonths(1)));

        // Customer 3: High Risk (2 Missed/Late payments)
        Customer c3 = new Customer("Anand Verma", "9765432109", "anand@example.com", "Business", 1000000.0, 35000.0);
        customerRepository.save(c3);
        emiPaymentRepository.save(new EmiPayment(c3, LocalDate.now().minusMonths(3), null));
        emiPaymentRepository.save(new EmiPayment(c3, LocalDate.now().minusMonths(2), LocalDate.now().minusMonths(2).plusDays(10)));
        emiPaymentRepository.save(new EmiPayment(c3, LocalDate.now().minusMonths(1), LocalDate.now().minusMonths(1)));

        System.out.println("DataInitializer: Sample customer data loaded successfully.");
    }
}
