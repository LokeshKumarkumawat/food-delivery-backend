package com.bytebyteboot.foodapp.cart.controller;

import com.bytebyteboot.foodapp.cart.dtos.CartDTO;
import com.bytebyteboot.foodapp.cart.services.CartService;
import com.bytebyteboot.foodapp.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<Response<?>> addItemToCart(@RequestBody CartDTO cartDTO){
        return ResponseEntity.ok(cartService.addItemToCart(cartDTO));
    }

    @PutMapping("/items/increment/{menuId}")
    public ResponseEntity<Response<?>> incrementItem(@PathVariable Long menuId){
        return ResponseEntity.ok(cartService.incrementItem(menuId));
    }

    @PutMapping("/items/decrement/{menuId}")
    public ResponseEntity<Response<?>> decrementItem(@PathVariable Long menuId){
        return ResponseEntity.ok(cartService.decrementItem(menuId));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Response<?>> removeItem(@PathVariable Long cartItemId){
        return ResponseEntity.ok(cartService.removeItem(cartItemId));
    }


    @GetMapping
    public ResponseEntity<Response<CartDTO>> getShoppingCart(){
        return ResponseEntity.ok(cartService.getShoppingCart());
    }

    @DeleteMapping
    public ResponseEntity<Response<?>> clearShoppingCart(){
        return ResponseEntity.ok(cartService.clearShoppingCart());
    }



}
