package com.bytebyteboot.foodapp.ratelimit;

import com.bytebyteboot.foodapp.ratelimiter.RateLimitType;
import com.bytebyteboot.foodapp.ratelimiter.config.RateLimitProperties;
import com.bytebyteboot.foodapp.ratelimiter.service.RateLimitService;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Rate Limit Service Tests")
class RateLimitServiceTest {

    private RateLimitService rateLimitService;
    private RateLimitProperties properties;

    @BeforeEach
    void setUp() {
        properties = new RateLimitProperties();
        properties.setAuthCapacity(5);
        properties.setAuthRefillRate(5);
        properties.setGeneralCapacity(100);
        properties.setGeneralRefillRate(100);

        rateLimitService = new RateLimitService(properties);
    }

    @Test
    @DisplayName("Should allow requests within limit")
    void testTryConsume_WithinLimit() {
        // Given
        String key = "test-user";
        RateLimitType type = RateLimitType.AUTH;

        // When & Then - Should allow 5 requests
        for (int i = 0; i < 5; i++) {
            assertThat(rateLimitService.tryConsume(key, type)).isTrue();
        }
    }

    @Test
    @DisplayName("Should block requests exceeding limit")
    void testTryConsume_ExceedsLimit() {
        // Given
        String key = "test-user";
        RateLimitType type = RateLimitType.AUTH;

        // When - Consume all 5 tokens
        for (int i = 0; i < 5; i++) {
            rateLimitService.tryConsume(key, type);
        }

        // Then - 6th request should be blocked
        assertThat(rateLimitService.tryConsume(key, type)).isFalse();
    }

    @Test
    @DisplayName("Should resolve different buckets for different keys")
    void testResolveBucket_DifferentKeys() {
        // Given
        String key1 = "user1";
        String key2 = "user2";
        RateLimitType type = RateLimitType.GENERAL;

        // When
        Bucket bucket1 = rateLimitService.resolveBucket(key1, type);
        Bucket bucket2 = rateLimitService.resolveBucket(key2, type);

        // Then
        assertThat(bucket1).isNotSameAs(bucket2);
    }

    @Test
    @DisplayName("Should return correct available tokens")
    void testGetAvailableTokens() {
        // Given
        String key = "test-user";
        RateLimitType type = RateLimitType.AUTH;

        // When
        rateLimitService.tryConsume(key, type);
        rateLimitService.tryConsume(key, type);
        long available = rateLimitService.getAvailableTokens(key, type);

        // Then
        assertThat(available).isEqualTo(3); // 5 - 2 = 3
    }

    @Test
    @DisplayName("Should apply different limits for different types")
    void testDifferentLimitsForDifferentTypes() {
        // Given
        String key = "test-user";

        // When & Then - AUTH should allow 5
        for (int i = 0; i < 5; i++) {
            assertThat(rateLimitService.tryConsume(key, RateLimitType.AUTH)).isTrue();
        }
        assertThat(rateLimitService.tryConsume(key, RateLimitType.AUTH)).isFalse();

        // GENERAL should still allow requests (different bucket)
        assertThat(rateLimitService.tryConsume(key, RateLimitType.GENERAL)).isTrue();
    }
}
