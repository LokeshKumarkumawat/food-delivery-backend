package com.bytebyteboot.foodapp.email_notification.services;


import com.bytebyteboot.foodapp.email_notification.dtos.NotificationDTO;

public interface NotificationService {
    void sendEmail(NotificationDTO notificationDTO);
}

