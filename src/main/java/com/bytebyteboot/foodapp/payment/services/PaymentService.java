package com.bytebyteboot.foodapp.payment.services;

import com.bytebyteboot.foodapp.payment.dtos.PaymentDTO;
import com.bytebyteboot.foodapp.response.Response;

import java.util.List;

public interface PaymentService {

    Response<?> initializePayment(PaymentDTO paymentDTO);
    void updatePaymentForOrder(PaymentDTO paymentDTO);
    Response<List<PaymentDTO>> getAllPayments();
    Response<PaymentDTO> getPaymentById(Long paymentId);

}