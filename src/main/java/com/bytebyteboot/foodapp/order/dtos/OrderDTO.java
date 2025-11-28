package com.bytebyteboot.foodapp.order.dtos;

import com.bytebyteboot.foodapp.auth_users.dtos.UserDTO;
import com.bytebyteboot.foodapp.enums.OrderStatus;
import com.bytebyteboot.foodapp.enums.PaymentStatus;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderDTO {


    private Long id;

    private LocalDateTime orderDate;

    private BigDecimal totalAmount;

    private OrderStatus orderStatus;

    private PaymentStatus paymentStatus;

    private UserDTO user; // Customer who is making/made the order

    private List<OrderItemDTO> orderItems;
}
