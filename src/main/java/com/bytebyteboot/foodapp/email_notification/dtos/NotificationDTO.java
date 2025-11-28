package com.bytebyteboot.foodapp.email_notification.dtos;

import com.bytebyteboot.foodapp.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class NotificationDTO {

    private Long id;

    private String subject;

    @NotBlank(message = "recipient is required")
    private String recipient; // Store the email address of the recipient

    private String body;

    private NotificationType type;

    private LocalDateTime createdAt;

    private boolean isHtml;
}
