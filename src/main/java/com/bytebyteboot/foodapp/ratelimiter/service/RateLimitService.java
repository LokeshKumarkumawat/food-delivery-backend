package com.bytebyteboot.foodapp.ratelimiter.service;

import com.bytebyteboot.foodapp.ratelimiter.RateLimitType;
import com.bytebyteboot.foodapp.ratelimiter.config.RateLimitProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private final RateLimitProperties rateLimitProperties;

    // Store buckets per IP address and rate limit type
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Resolve bucket for specific key and type
     */
    public Bucket resolveBucket(String key, RateLimitType type) {
        String bucketKey = type.name() + ":" + key;
        return buckets.computeIfAbsent(bucketKey, k -> createBucket(type));
    }


    /**
     * Create bucket based on rate limit type
     */
    private Bucket createBucket(RateLimitType type) {
        Bandwidth limit;

        switch (type) {
            case AUTH:
                limit = Bandwidth.classic(
                        rateLimitProperties.getAuthCapacity(),
                        Refill.intervally(
                                rateLimitProperties.getAuthRefillRate(),
                                Duration.ofMinutes(1)
                        )
                );
                break;

            case WRITE:
                limit = Bandwidth.classic(
                        rateLimitProperties.getWriteCapacity(),
                        Refill.intervally(
                                rateLimitProperties.getWriteRefillRate(),
                                Duration.ofMinutes(1)
                        )
                );
                break;

            case ADMIN:
                limit = Bandwidth.classic(
                        rateLimitProperties.getAdminCapacity(),
                        Refill.intervally(
                                rateLimitProperties.getAdminRefillRate(),
                                Duration.ofMinutes(1)
                        )
                );
                break;

            case UPLOAD:
                limit = Bandwidth.classic(
                        rateLimitProperties.getUploadCapacity(),
                        Refill.intervally(
                                rateLimitProperties.getUploadRefillRate(),
                                Duration.ofMinutes(1)
                        )
                );
                break;

            case GENERAL:
            default:
                limit = Bandwidth.classic(
                        rateLimitProperties.getGeneralCapacity(),
                        Refill.intervally(
                                rateLimitProperties.getGeneralRefillRate(),
                                Duration.ofMinutes(1)
                        )
                );
                break;
        }

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Try to consume token from bucket
     */
    public boolean tryConsume(String key, RateLimitType type) {
        Bucket bucket = resolveBucket(key, type);
        boolean consumed = bucket.tryConsume(1);

        if (!consumed) {
            log.warn("Rate limit exceeded for key: {} with type: {}", key, type);
        }

        return consumed;
    }

    /**
     * Get available tokens
     */
    public long getAvailableTokens(String key, RateLimitType type) {
        Bucket bucket = resolveBucket(key, type);
        return bucket.getAvailableTokens();
    }



    public Map<String, Bucket> getBuckets() {
        return buckets;
    }



}
