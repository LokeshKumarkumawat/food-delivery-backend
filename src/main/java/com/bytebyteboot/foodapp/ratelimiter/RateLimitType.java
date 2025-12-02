package com.bytebyteboot.foodapp.ratelimiter;

public enum RateLimitType {
    GENERAL,    // General API requests
    AUTH,       // Login, Register
    WRITE,      // Create, Update, Delete
    ADMIN,      // Admin operations
    UPLOAD      // File uploads
}
