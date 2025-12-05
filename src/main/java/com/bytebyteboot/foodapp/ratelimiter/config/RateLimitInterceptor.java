package com.bytebyteboot.foodapp.ratelimiter.config;

import com.bytebyteboot.foodapp.exceptions.RateLimitExceededException;
import com.bytebyteboot.foodapp.ratelimiter.RateLimit;
import com.bytebyteboot.foodapp.ratelimiter.RateLimitType;
import com.bytebyteboot.foodapp.ratelimiter.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;
    private final RateLimitProperties rateLimitProperties;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);

        if (rateLimit == null) {
            return true; // No rate limit applied
        }

        // Get identifier (IP or User ID)
        String identifier = getIdentifier(request);
        RateLimitType type = rateLimit.type();

//        // Check rate limit
//        if (!rateLimitService.tryConsume(identifier, type)) {
//            long availableTokens = rateLimitService.getAvailableTokens(identifier, type);
//
//            // Add rate limit headers
//            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(availableTokens));
//            response.setHeader("X-Rate-Limit-Retry-After-Seconds", "60");
//
//            throw new RateLimitExceededException(
//                    "Rate limit exceeded. Please try again later."
//            );
//        }

        // Try consume token
        boolean consumed = rateLimitService.tryConsume(identifier, type);
        long availableTokens = rateLimitService.getAvailableTokens(identifier, type);

        int capacity = getCapacity(type);   // NEW
        long resetTime = System.currentTimeMillis() / 1000 + 60;  // reset after 60 sec

        // ALWAYS add these headers (best practice)
        response.setHeader("X-Rate-Limit-Limit", String.valueOf(capacity));
        response.setHeader("X-Rate-Limit-Remaining", String.valueOf(availableTokens));
        response.setHeader("X-Rate-Limit-Reset", String.valueOf(resetTime));

        if (!consumed) {
            // ONLY when limit exceeded
            response.setHeader("X-Rate-Limit-Retry-After-Seconds", "60");

            throw new RateLimitExceededException(
                    "Rate limit exceeded. Please try again later."
            );
        }

        return true;

    }

    private int getCapacity(RateLimitType type) {
        switch (type) {
            case AUTH: return rateLimitProperties.getAuthCapacity();
            case WRITE: return rateLimitProperties.getWriteCapacity();
            case ADMIN: return rateLimitProperties.getAdminCapacity();
            case UPLOAD: return rateLimitProperties.getUploadCapacity();
            default: return rateLimitProperties.getGeneralCapacity();
        }
    }

    /**
     * Get identifier from request (authenticated user or IP address)
     */
    private String getIdentifier(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            // Use username for authenticated users
            return authentication.getName();
        }

        // Use IP address for unauthenticated users
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}