package com.bytebyteboot.foodapp.cart.dtos;

import com.bytebyteboot.foodapp.menu.dtos.MenuDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Individual cart item")
public class CartItemDTO {

    @Schema(description = "Cart item ID", example = "1")
    private Long id;

    @Schema(description = "Menu item details")
    private MenuDTO menu;

    @Schema(description = "Quantity", example = "2")
    private int quantity;

    @Schema(description = "Price per unit", example = "15.99")
    private BigDecimal pricePerUnit;

    @Schema(description = "Subtotal (quantity × price)", example = "31.98")
    private BigDecimal subtotal;
}
