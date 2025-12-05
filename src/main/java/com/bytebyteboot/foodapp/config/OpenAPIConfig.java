package com.bytebyteboot.foodapp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${app.name:Food Delivery API}")
    private String appName;

    @Value("${app.description:Complete Food Delivery Management System}")
    private String appDescription;

    @Value("${server.url:http://localhost:8090}")
    private String serverUrl;

    @Value("${server.prod.url:https://api.foodapp.com}")
    private String prodServerUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(servers())
                .components(securityScheme())
                .security(List.of(securityRequirement()))
                .tags(apiTags());
    }

    private Info apiInfo() {
        return new Info()
                .title(appName)
                .version(appVersion)
                .description(appDescription + "\n\n" +
                        "## Features\n" +
                        "- 🔐 User Authentication & Authorization\n" +
                        "- 🛒 Shopping Cart Management\n" +
                        "- 📦 Order Processing\n" +
                        "- 💳 Payment Integration\n" +
                        "- ⭐ Review & Rating System\n" +
                        "- 🚀 Rate Limiting\n" +
                        "- 💾 Redis Caching\n\n" +
                        "## Authentication\n" +
                        "Most endpoints require JWT Bearer token authentication. " +
                        "Use the `/api/auth/login` endpoint to obtain a token.")
                .contact(apiContact())
                .license(apiLicense());
    }

    private Contact apiContact() {
        return new Contact()
                .name("Food App Support Team")
                .email("support@foodapp.com")
                .url("https://www.foodapp.com/support");
    }

    private License apiLicense() {
        return new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0");
    }

    private List<Server> servers() {
        Server devServer = new Server()
                .url(serverUrl)
                .description("Development Server");

        Server prodServer = new Server()
                .url(prodServerUrl)
                .description("Production Server");

        return List.of(devServer, prodServer);
    }

    private Components securityScheme() {
        return new Components()
                .addSecuritySchemes("Bearer Authentication",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token obtained from login endpoint")
                );
    }

    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement()
                .addList("Bearer Authentication");
    }

    private List<Tag> apiTags() {
        return List.of(
                new Tag().name("Authentication")
                        .description("Endpoints for user registration, login, and authentication"),
                new Tag().name("Users")
                        .description("User profile management and administration"),
                new Tag().name("Categories")
                        .description("Food category management (Admin only)"),
                new Tag().name("Menus")
                        .description("Menu item management and browsing"),
                new Tag().name("Cart")
                        .description("Shopping cart operations"),
                new Tag().name("Orders")
                        .description("Order placement and management"),
                new Tag().name("Payments")
                        .description("Payment processing and history"),
                new Tag().name("Reviews")
                        .description("Product reviews and ratings"),
                new Tag().name("Roles")
                        .description("User role management (Admin only)")
        );
    }
}