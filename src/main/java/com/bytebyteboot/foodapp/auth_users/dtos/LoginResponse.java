package com.bytebyteboot.foodapp.auth_users.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Login response with JWT token and user roles")
public class LoginResponse {

    @Schema(
            description = "JWT Bearer token (use in Authorization header)",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTYxNjIzOTAyMn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
    )
    private String token;

    @Schema(
            description = "List of user roles",
            example = "[\"CUSTOMER\"]"
    )
    private List<String> roles;

}
