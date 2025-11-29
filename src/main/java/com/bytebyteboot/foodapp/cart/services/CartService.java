package com.bytebyteboot.foodapp.cart.services;

import com.bytebyteboot.foodapp.cart.dtos.CartDTO;
import com.bytebyteboot.foodapp.response.Response;


public interface CartService {

    Response<?> addItemToCart(CartDTO cartDTO);
    Response<?> incrementItem(Long menuId);
    Response<?> decrementItem(Long menuId);
    Response<?> removeItem(Long cartItemId);
    Response<CartDTO> getShoppingCart();
    Response<?> clearShoppingCart();
}