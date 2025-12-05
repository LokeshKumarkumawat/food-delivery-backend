package com.bytebyteboot.foodapp.ratelimiter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rate.limit")
@Data
public class RateLimitProperties {
    // General API requests
    private int generalCapacity = 100;  // requests per minute
    private int generalRefillRate = 100;

    // Authentication endpoints (stricter)
    private int authCapacity = 3;  // requests per minute
    private int authRefillRate = 3;

    // Write operations (create, update, delete)
    private int writeCapacity = 20;
    private int writeRefillRate = 20;

    // Admin operations
    private int adminCapacity = 50;
    private int adminRefillRate = 50;

    // File upload operations
    private int uploadCapacity = 10;
    private int uploadRefillRate = 10;

}
