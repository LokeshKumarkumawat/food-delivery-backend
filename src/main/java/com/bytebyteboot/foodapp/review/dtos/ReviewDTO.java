package com.bytebyteboot.foodapp.review.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Product review and rating")
public class ReviewDTO {

    @Schema(description = "Review ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Menu item ID being reviewed", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long menuId;

    @Schema(description = "Order ID (must be delivered)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long orderId;

    @Schema(description = "Reviewer name", accessMode = Schema.AccessMode.READ_ONLY)
    private String userName;

    @NotNull(message = "Rating is required")
    @Min(1)
    @Max(10)
    @Schema(description = "Rating (1-10 stars)", example = "8", required = true, minimum = "1", maximum = "10")
    private Integer rating;

    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    @Schema(description = "Review comment", example = "Excellent pizza! Fresh ingredients and great taste.", maxLength = 500)
    private String comment;

    @Schema(description = "Menu item name", accessMode = Schema.AccessMode.READ_ONLY)
    private String menuName;

    @Schema(description = "Review date and time", example = "2024-12-04T12:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
}
