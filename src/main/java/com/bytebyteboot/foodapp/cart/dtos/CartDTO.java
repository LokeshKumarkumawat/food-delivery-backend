package com.bytebyteboot.foodapp.cart.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Shopping cart details")
public class CartDTO {

    @Schema(
            description = "Cart ID",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @Schema(
            description = "List of cart items",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private List<CartItemDTO> cartItems;


    @Schema(
            description = "Menu item ID to add to cart",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long menuId;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(
            description = "Quantity of items",
            example = "2",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "1"
    )
    private int quantity;


    @Schema(
            description = "Total cart amount",
            example = "45.98",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private BigDecimal totalAmount;

}