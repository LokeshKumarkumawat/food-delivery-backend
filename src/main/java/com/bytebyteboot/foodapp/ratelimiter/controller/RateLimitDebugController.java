package com.bytebyteboot.foodapp.ratelimiter.controller;

import com.bytebyteboot.foodapp.ratelimiter.RateLimitType;
import com.bytebyteboot.foodapp.ratelimiter.config.RateLimitProperties;
import com.bytebyteboot.foodapp.ratelimiter.service.RateLimitService;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/internal/debug/ratelimit")
@RequiredArgsConstructor
public class RateLimitDebugController {

    private final RateLimitService rateLimitService;
    private final RateLimitProperties rateLimitProperties;

    @GetMapping("/buckets")
    public List<BucketView> getAllBuckets() {
        List<BucketView> result = new ArrayList<>();

        for (Map.Entry<String, Bucket> entry : rateLimitService.getBuckets().entrySet()) {
            String key = entry.getKey();            // e.g. "GENERAL:192.168.0.10"
            Bucket bucket = entry.getValue();

            String[] parts = key.split(":", 2);
            String typeName = parts[0];
            String identifier = parts.length > 1 ? parts[1] : "";

            RateLimitType type = RateLimitType.valueOf(typeName);

            int capacity = getCapacity(type);
            long availableTokens = bucket.getAvailableTokens();

            result.add(new BucketView(
                    type.name(),
                    identifier,
                    capacity,
                    availableTokens
            ));
        }

        return result;
    }

    private int getCapacity(RateLimitType type) {
        return switch (type) {
            case AUTH -> rateLimitProperties.getAuthCapacity();
            case WRITE -> rateLimitProperties.getWriteCapacity();
            case ADMIN -> rateLimitProperties.getAdminCapacity();
            case UPLOAD -> rateLimitProperties.getUploadCapacity();
            case GENERAL -> rateLimitProperties.getGeneralCapacity();
        };
    }

    public record BucketView(
            String type,
            String identifier,
            int capacity,
            long availableTokens
    ) { }
}
