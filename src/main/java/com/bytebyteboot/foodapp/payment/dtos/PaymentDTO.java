package com.bytebyteboot.foodapp.payment.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.bytebyteboot.foodapp.auth_users.dtos.UserDTO;
import com.bytebyteboot.foodapp.enums.PaymentGateway;
import com.bytebyteboot.foodapp.enums.PaymentStatus;
import com.bytebyteboot.foodapp.order.dtos.OrderDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Payment details")
public class PaymentDTO {

    @Schema(description = "Payment ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Order ID for this payment", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long orderId;

    @Schema(description = "Payment amount", example = "52.47", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @Schema(description = "Payment status", example = "COMPLETED", allowableValues = {"PENDING", "COMPLETED", "FAILED"})
    private PaymentStatus paymentStatus;

    @Schema(description = "Transaction ID from payment gateway", example = "txn_1234567890")
    private String transactionId;

    @Schema(description = "Payment gateway", example = "STRIPE", allowableValues = {"STRIPE", "PAYPAL", "RAZORPAY"})
    private PaymentGateway paymentGateway;

    @Schema(description = "Failure reason if payment failed", example = "Insufficient funds")
    private String failureReason;

    @Schema(description = "Payment success indicator", example = "true")
    private boolean success;

    @Schema(description = "Payment date and time", example = "2024-12-04T10:35:00")
    private LocalDateTime paymentDate;

    private OrderDTO order;
    private UserDTO user;
}
