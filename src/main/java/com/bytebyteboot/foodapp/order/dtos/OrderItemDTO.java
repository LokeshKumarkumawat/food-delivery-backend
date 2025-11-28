package com.bytebyteboot.foodapp.order.dtos;

import com.bytebyteboot.foodapp.menu.dtos.MenuDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderItemDTO {

    private Long id;

    private int quantity;

    private Long menuId;

    private MenuDTO menu;

    private BigDecimal pricePerUnit;

    private BigDecimal subtotal;
}
