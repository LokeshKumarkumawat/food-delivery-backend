package com.bytebyteboot.foodapp.menu.dtos;


import com.bytebyteboot.foodapp.review.dtos.ReviewDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Menu item details")
public class MenuDTO {

    @Schema(
            description = "Menu item ID",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    @Schema(
            description = "Menu item name",
            example = "Margherita Pizza",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;

    @Schema(
            description = "Detailed description of the menu item",
            example = "Classic Italian pizza with tomato sauce, mozzarella, and fresh basil"
    )
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
//    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
//    @DecimalMax(value = "9999.99", message = "Price must be less than 10000")
    @Schema(
            description = "Price in USD",
            example = "15.99",
            requiredMode = Schema.RequiredMode.REQUIRED
//            minimum = "0.01",
//            maximum = "9999.99"
    )
    private BigDecimal price;

    @Schema(
            description = "Menu item image URL (auto-generated after upload)",
            example = "https://s3.amazonaws.com/foodapp/menus/pizza123.jpg",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String imageUrl;

    @NotNull(message = "Category ID is required")
    @Schema(
            description = "Category ID this menu item belongs to",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long categoryId; // needed when adding a menu

    @Schema(
            description = "Image file to upload (JPEG, PNG, max 5MB)",
            type = "string",
            format = "binary",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private MultipartFile imageFile; // For uploading the image

    @Schema(
            description = "List of customer reviews",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private List<ReviewDTO> reviews;

}
