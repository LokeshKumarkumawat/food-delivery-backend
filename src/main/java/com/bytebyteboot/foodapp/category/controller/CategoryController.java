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

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @RateLimit(type = RateLimitType.WRITE)  // 20 requests per minute
    public ResponseEntity<Response<CategoryDTO>> addCategory(@RequestBody @Valid CategoryDTO categoryDTO){
        return ResponseEntity.ok(categoryService.addCategory(categoryDTO));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @RateLimit(type = RateLimitType.WRITE)
    public ResponseEntity<Response<CategoryDTO>> updateCategory(@RequestBody CategoryDTO categoryDTO){
        return ResponseEntity.ok(categoryService.updateCategory(categoryDTO));
    }


    @GetMapping("/{id}")
    @RateLimit(type = RateLimitType.GENERAL)  // 100 requests per minute
    public ResponseEntity<Response<CategoryDTO>> getCategoryById(@PathVariable Long id){
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @GetMapping("/all")
    @RateLimit(type = RateLimitType.GENERAL)
    public ResponseEntity<Response<List<CategoryDTO>>> getAllCategories(){
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @RateLimit(type = RateLimitType.WRITE)
    public ResponseEntity<Response<?>> deleteCategory(@PathVariable Long id){
        return ResponseEntity.ok(categoryService.deleteCategory(id));
    }

}
