package com.bytebyteboot.foodapp.payment.repository;

import com.bytebyteboot.foodapp.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
