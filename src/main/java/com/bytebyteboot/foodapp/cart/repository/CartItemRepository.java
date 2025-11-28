package com.bytebyteboot.foodapp.cart.repository;

import com.bytebyteboot.foodapp.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
