package com.bytebyteboot.foodapp.order.dtos;

import com.bytebyteboot.foodapp.auth_users.dtos.UserDTO;
import com.bytebyteboot.foodapp.enums.OrderStatus;
import com.bytebyteboot.foodapp.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Order details")
public class OrderDTO {


    @Schema(description = "Order ID", example = "1")
    private Long id;

    @Schema(description = "Order date and time", example = "2024-12-04T10:30:00")
    private LocalDateTime orderDate;

    @Schema(description = "Total order amount", example = "52.47")
    private BigDecimal totalAmount;

    @Schema(
            description = "Order status",
            example = "CONFIRMED",
            allowableValues = {"INITIALIZED", "CONFIRMED", "PREPARING", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED"}
    )
    private OrderStatus orderStatus;

    @Schema(
            description = "Payment status",
            example = "COMPLETED",
            allowableValues = {"PENDING", "COMPLETED", "FAILED"}
    )
    private PaymentStatus paymentStatus;

    @Schema(description = "Customer information")
    private UserDTO user; // Customer who is making/made the order

    @Schema(description = "List of ordered items")
    private List<OrderItemDTO> orderItems;
}
