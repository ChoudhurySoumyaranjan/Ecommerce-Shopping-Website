package com.shopify.main.service;

import com.shopify.main.entity.Cart;
import com.shopify.main.entity.Product;
import com.shopify.main.entity.User;
import com.shopify.main.repository.CartRepository;
import com.shopify.main.repository.ProductRepository;
import com.shopify.main.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements  CartService{

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Cart saveCart(Long pid, Long uid) {
        Product product = productRepository.findById(pid).get();
        User user = userRepository.findById(uid).get();

        Optional<Cart> optionalCart=cartRepository.findByProductIdAndUserId(pid,uid);

        Cart newCart =null;

        if (optionalCart.isEmpty()){
            newCart = new Cart();
            newCart.setQuantity(1);
            newCart.setPrice(product.getDiscountedPrice());
            newCart.setUser(user);
            newCart.setProduct(product);
        }else {
            Cart existingCart=optionalCart.get();

            newCart=existingCart;
            newCart.setQuantity(newCart.getQuantity()+1);
            newCart.setPrice(existingCart.getQuantity()*existingCart.getProduct().getDiscountedPrice());
        }



        return cartRepository.save(newCart);
    }

    @Override
    public Integer getUserTotalCartItems(Long userId) {
        return cartRepository.sumQuantityByUserId(userId);
    }

    @Override
    public List<Cart> getAllCartProducts(Long userId) {
        return cartRepository.findAllByUserId(userId);
    }

    @Override
    public void deleteCartItem(Cart cart) {
        cartRepository.delete(cart);
    }

    @Override
    public boolean decreaseCartItemQuantity(Cart cart) {
        try{

            Cart existingCart=cartRepository.findById(cart.getId()).get();

            int existingQuantity=existingCart.getQuantity();

            existingCart.setQuantity(existingQuantity-1);

            cartRepository.save(existingCart);

            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean increaseCartItemQuantity(Cart cart) {

        try{

            Cart existingCart=cartRepository.findById(cart.getId()).get();

            existingCart.setQuantity(existingCart.getQuantity()+1);

            cartRepository.save(existingCart);

            return  true;

        }catch (Exception e){
            e.printStackTrace();
            return  false;
        }
    }
}
