package com.example.loanrisk.repository;

import com.example.loanrisk.entity.EmiPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmiPaymentRepository extends JpaRepository<EmiPayment, Long> {
    List<EmiPayment> findByCustomerIdOrderByDueDateDesc(Long customerId);
}
