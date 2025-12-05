package com.bytebyteboot.foodapp.auth_users.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "User registration request")
public class RegistrationRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Schema(
            description = "User's full name",
            example = "John Doe",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 2,
            maxLength = 100
    )
    private String name;

    @NotBlank(message = "Email is required")
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Schema(
            description = "User's email address (must be unique)",
            example = "john.doe@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED,
            format = "email"
    )
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 3, message = "Password must be at least 3 characters long")
    @Schema(
            description = "User's password (minimum 8 characters)",
            example = "SecurePass123!",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 3,
            maxLength = 100,
            format = "password"
    )
    private String password;

    @NotBlank(message = "Address is required")
    @Schema(
            description = "User's delivery address",
            example = "123 Main St, Apartment 4B, New York, NY 10001"
    )
    private String address;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be 10-15 digits")
    @Schema(
            description = "User's phone number (10-15 digits)",
            example = "1234567890",
            pattern = "^[0-9]{10,15}$"
    )
    private String phoneNumber;

    @Schema(
            description = "User roles (default: CUSTOMER). Available: CUSTOMER, ADMIN, DELIVERY_AGENT",
            example = "[\"CUSTOMER\"]",
            allowableValues = {"CUSTOMER", "ADMIN", "DELIVERY_AGENT"}
    )
    private List<String> roles;
}
