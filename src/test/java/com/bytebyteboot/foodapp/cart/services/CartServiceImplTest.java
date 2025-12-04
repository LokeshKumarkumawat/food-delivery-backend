package com.bytebyteboot.foodapp.cart.services;

import com.bytebyteboot.foodapp.auth_users.entity.User;
import com.bytebyteboot.foodapp.auth_users.services.UserService;
import com.bytebyteboot.foodapp.cart.dtos.CartDTO;
import com.bytebyteboot.foodapp.cart.entity.Cart;
import com.bytebyteboot.foodapp.cart.entity.CartItem;
import com.bytebyteboot.foodapp.cart.repository.CartItemRepository;
import com.bytebyteboot.foodapp.cart.repository.CartRepository;
import com.bytebyteboot.foodapp.exceptions.NotFoundException;
import com.bytebyteboot.foodapp.menu.entity.Menu;
import com.bytebyteboot.foodapp.menu.repository.MenuRepository;
import com.bytebyteboot.foodapp.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Cart Service Tests")
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    private User user;
    private Cart cart;
    private Menu menu;
    private CartItem cartItem;
    private CartDTO cartDTO;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        menu = Menu.builder()
                .id(1L)
                .name("Pizza")
                .price(BigDecimal.valueOf(15.99))
                .build();

        cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        cart.setCartItems(new ArrayList<>());

        cartItem = CartItem.builder()
                .id(1L)
                .cart(cart)
                .menu(menu)
                .quantity(2)
                .pricePerUnit(BigDecimal.valueOf(15.99))
                .subtotal(BigDecimal.valueOf(31.98))
                .build();

        cartDTO = new CartDTO();
        cartDTO.setMenuId(1L);
        cartDTO.setQuantity(2);
    }

    @Test
    @DisplayName("Should add item to cart successfully")
    void testAddItemToCart_Success() {
        // Given
        when(userService.getCurrentLoggedInUser()).thenReturn(user);
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(cartRepository.findByUser_Id(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);

        // When
        Response<?> response = cartService.addItemToCart(cartDTO);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("Item added to cart successfully");

        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    @DisplayName("Should throw exception when menu not found")
    void testAddItemToCart_MenuNotFound() {
        // Given
        when(userService.getCurrentLoggedInUser()).thenReturn(user);
        when(menuRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cartService.addItemToCart(cartDTO))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Menu Item Not Found");
    }

    @Test
    @DisplayName("Should increment cart item successfully")
    void testIncrementItem_Success() {
        // Given
        cart.getCartItems().add(cartItem);
        when(userService.getCurrentLoggedInUser()).thenReturn(user);
        when(cartRepository.findByUser_Id(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);

        // When
        Response<?> response = cartService.incrementItem(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(cartItem.getQuantity()).isEqualTo(3);

        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    @DisplayName("Should clear shopping cart successfully")
    void testClearShoppingCart_Success() {
        // Given
        cart.getCartItems().add(cartItem);
        when(userService.getCurrentLoggedInUser()).thenReturn(user);
        when(cartRepository.findByUser_Id(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // When
        Response<?> response = cartService.clearShoppingCart();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(cart.getCartItems()).isEmpty();

        verify(cartItemRepository).deleteAll(anyList());
        verify(cartRepository).save(cart);
    }
}