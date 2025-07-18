package com.shopify.main.service;

import com.shopify.main.entity.Cart;

import java.util.List;
import java.util.Optional;

public interface CartService {
    Cart saveCart(Long pid,Long uid);

    Integer getUserTotalCartItems(Long userId);

    List<Cart> getAllCartProducts(Long userId);

    void deleteCartItem(Cart cart);

    boolean decreaseCartItemQuantity(Cart cart);

    boolean increaseCartItemQuantity(Cart cart);

}
