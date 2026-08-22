package com.matrix.payment_service.repository;

import com.matrix.payment_service.entity.Payment;
import com.matrix.payment_service.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment,String> {
    List<Payment> findByStatus(PaymentStatus status);

}
