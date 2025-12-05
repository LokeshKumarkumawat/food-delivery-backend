package com.bytebyteboot.foodapp.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API response wrapper")
public class Response<T> {
    @Schema(description = "HTTP status code", example = "200")
    private int statusCode; // e.g., "200", "404"

    @Schema(description = "Response message", example = "Operation successful")
    private String message; // Additional information about the response

    @Schema(description = "Response data (varies by endpoint)")
    private T data; // The actual data payload

    private Map<String, Serializable> meta;
}
