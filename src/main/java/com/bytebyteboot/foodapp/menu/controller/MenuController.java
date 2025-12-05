package com.bytebyteboot.foodapp.menu.controller;

import com.bytebyteboot.foodapp.menu.dtos.MenuDTO;
import com.bytebyteboot.foodapp.menu.services.MenuService;
import com.bytebyteboot.foodapp.ratelimiter.RateLimit;
import com.bytebyteboot.foodapp.ratelimiter.RateLimitType;
import com.bytebyteboot.foodapp.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
@Tag(name = "Menus", description = "Menu item management APIs")
public class MenuController {

    private final MenuService menuService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    @RateLimit(type = RateLimitType.UPLOAD)  // 10 requests per minute (file upload)
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Create menu item with image (Admin only)",
            description = "Create a new menu item with image upload. Requires ADMIN role. Image is required."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Menu created successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Image required or validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Response<MenuDTO>> createMenu(
            @ModelAttribute @Valid MenuDTO menuDTO,
            @RequestPart(value = "imageFile", required = true) MultipartFile imageFile) {

        menuDTO.setImageFile(imageFile);
        return ResponseEntity.ok(menuService.createMenu(menuDTO));
    }


    @PutMapping( consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    @RateLimit(type = RateLimitType.UPLOAD)
    public ResponseEntity<Response<MenuDTO>> updateMenu(
            @ModelAttribute MenuDTO menuDTO,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {

        menuDTO.setImageFile(imageFile);  // Attach the imageFile to the DTO
        return ResponseEntity.ok(menuService.updateMenu(menuDTO));
    }


    @GetMapping("/{id}")
    @RateLimit(type = RateLimitType.GENERAL)
    @SecurityRequirements
    @Operation(
            summary = "Get menu item by ID",
            description = "Retrieve detailed information about a specific menu item including reviews"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Menu found"),
            @ApiResponse(responseCode = "404", description = "Menu not found")
    })
    public ResponseEntity<Response<MenuDTO>> getMenuById(
            @Parameter(description = "Menu ID", example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(menuService.getMenuById(id));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @RateLimit(type = RateLimitType.WRITE)
    public ResponseEntity<Response<?>> deleteMenu(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.deleteMenu(id));
    }


    @GetMapping
    @RateLimit(type = RateLimitType.GENERAL)
    @SecurityRequirements
    @Operation(
            summary = "Get all menu items with filters",
            description = "Retrieve menu items with optional category and search filters"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Menus retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Response.class)
                    )
            )
    })
    public ResponseEntity<Response<List<MenuDTO>>> getMenus(
            @Parameter(description = "Filter by category ID", example = "1")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Search by name or description", example = "pizza")
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(menuService.getMenus(categoryId, search));
    }

}
