package com.bytebyteboot.foodapp.ratelimit;


import com.bytebyteboot.foodapp.exceptions.RateLimitExceededException;
import com.bytebyteboot.foodapp.ratelimiter.RateLimit;
import com.bytebyteboot.foodapp.ratelimiter.RateLimitType;
import com.bytebyteboot.foodapp.ratelimiter.config.RateLimitInterceptor;
import com.bytebyteboot.foodapp.ratelimiter.config.RateLimitProperties;
import com.bytebyteboot.foodapp.ratelimiter.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.method.HandlerMethod;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Rate Limit Interceptor Tests")
class RateLimitInterceptorTest {

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private RateLimitProperties rateLimitProperties;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HandlerMethod handlerMethod;

    @InjectMocks
    private RateLimitInterceptor interceptor;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Should allow request when rate limit not exceeded")
    void testPreHandle_RateLimitNotExceeded() throws Exception {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        when(rateLimitProperties.getGeneralCapacity()).thenReturn(100);
        // Given
        RateLimit rateLimit = mock(RateLimit.class);
        when(rateLimit.type()).thenReturn(RateLimitType.GENERAL);
        when(handlerMethod.getMethodAnnotation(RateLimit.class)).thenReturn(rateLimit);
        when(rateLimitService.tryConsume(anyString(), any(RateLimitType.class))).thenReturn(true);


        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // Then
        assertThat(result).isTrue();
        verify(rateLimitService).tryConsume(anyString(), eq(RateLimitType.GENERAL));
    }

    @Test
    @DisplayName("Should block request when rate limit exceeded")
    void testPreHandle_RateLimitExceeded() {

        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        when(rateLimitProperties.getAuthCapacity()).thenReturn(3);

        // Given
        RateLimit rateLimit = mock(RateLimit.class);
        when(rateLimit.type()).thenReturn(RateLimitType.AUTH);
        when(handlerMethod.getMethodAnnotation(RateLimit.class)).thenReturn(rateLimit);
        when(rateLimitService.tryConsume(anyString(), any(RateLimitType.class))).thenReturn(false);
        when(rateLimitService.getAvailableTokens(anyString(), any(RateLimitType.class))).thenReturn(0L);

        // When & Then
        assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerMethod))
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessage("Rate limit exceeded. Please try again later.");

        verify(response).setHeader("X-Rate-Limit-Remaining", "0");
        verify(response).setHeader("X-Rate-Limit-Retry-After-Seconds", "60");
    }

    @Test
    @DisplayName("Should pass through when no rate limit annotation")
    void testPreHandle_NoRateLimitAnnotation() throws Exception {
        // Given
        when(handlerMethod.getMethodAnnotation(RateLimit.class)).thenReturn(null);

        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // Then
        assertThat(result).isTrue();
        verify(rateLimitService, never()).tryConsume(anyString(), any(RateLimitType.class));
    }
}
