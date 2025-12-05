package com.bytebyteboot.foodapp.category.controller;

import com.bytebyteboot.foodapp.category.dtos.CategoryDTO;
import com.bytebyteboot.foodapp.category.services.CategoryService;
import com.bytebyteboot.foodapp.ratelimiter.RateLimit;
import com.bytebyteboot.foodapp.ratelimiter.RateLimitType;
import com.bytebyteboot.foodapp.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Food category management APIs")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @RateLimit(type = RateLimitType.WRITE)  // 20 requests per minute
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Create new category (Admin only)",
            description = "Create a new food category. Requires ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Category created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Response.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "429", description = "Too Many Requests")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Category details",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CategoryDTO.class),
                    examples = @ExampleObject(
                            value = """
                    {
                      "name": "Italian",
                      "description": "Authentic Italian cuisine including pasta, pizza, and more"
                    }
                    """
                    )
            )
    )
    public ResponseEntity<Response<CategoryDTO>> addCategory(@RequestBody @Valid CategoryDTO categoryDTO){
        return ResponseEntity.ok(categoryService.addCategory(categoryDTO));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @RateLimit(type = RateLimitType.WRITE)
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Update category (Admin only)",
            description = "Update an existing category. Requires ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Response<CategoryDTO>> updateCategory(@RequestBody CategoryDTO categoryDTO){
        return ResponseEntity.ok(categoryService.updateCategory(categoryDTO));
    }


    @GetMapping("/{id}")
    @RateLimit(type = RateLimitType.GENERAL)  // 100 requests per minute
    @SecurityRequirements
    @Operation(
            summary = "Get category by ID",
            description = "Retrieve a specific category by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Category found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Response.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Category not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"statusCode\":404,\"message\":\"Category Not Found\",\"data\":null}"
                            )
                    )
            )
    })
    public ResponseEntity<Response<CategoryDTO>> getCategoryById(@PathVariable Long id){
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @GetMapping("/all")
    @RateLimit(type = RateLimitType.GENERAL)
    @SecurityRequirements // Public endpoint
    @Operation(
            summary = "Get all categories",
            description = "Retrieve list of all food categories. No authentication required."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Categories retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Response.class),
                            examples = @ExampleObject(
                                    value = """
                        {
                          "statusCode": 200,
                          "message": "All categories retrieved successfully",
                          "data": [
                            {
                              "id": 1,
                              "name": "Italian",
                              "description": "Authentic Italian cuisine"
                            },
                            {
                              "id": 2,
                              "name": "Chinese",
                              "description": "Traditional Chinese dishes"
                            }
                          ]
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(responseCode = "429", description = "Too Many Requests")
    })
    public ResponseEntity<Response<List<CategoryDTO>>> getAllCategories(){
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @RateLimit(type = RateLimitType.WRITE)
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Delete category (Admin only)",
            description = "Delete a category by ID. Requires ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Response<?>> deleteCategory(@PathVariable Long id){
        return ResponseEntity.ok(categoryService.deleteCategory(id));
    }

}
