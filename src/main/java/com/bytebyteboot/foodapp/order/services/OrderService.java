package com.bytebyteboot.foodapp.order.services;

import com.bytebyteboot.foodapp.enums.OrderStatus;
import com.bytebyteboot.foodapp.order.dtos.OrderDTO;
import com.bytebyteboot.foodapp.order.dtos.OrderItemDTO;
import com.bytebyteboot.foodapp.response.Response;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderService {

    Response<?> placeOrderFromCart();
    Response<OrderDTO> getOrderById(Long id);
    Response<Page<OrderDTO>> getAllOrders(OrderStatus orderStatus, int page, int size);
    Response<List<OrderDTO>> getOrdersOfUser();
    Response<OrderItemDTO> getOrderItemById(Long orderItemId);
    Response<OrderDTO> updateOrderStatus(OrderDTO orderDTO);
    Response<Long> countUniqueCustomers();
}
