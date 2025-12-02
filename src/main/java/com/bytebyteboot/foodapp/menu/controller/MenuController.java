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

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    @RateLimit(type = RateLimitType.UPLOAD)  // 10 requests per minute (file upload)
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
    public ResponseEntity<Response<MenuDTO>> getMenuById(@PathVariable Long id) {
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
    public ResponseEntity<Response<List<MenuDTO>>> getMenus(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(menuService.getMenus(categoryId, search));
    }

}
