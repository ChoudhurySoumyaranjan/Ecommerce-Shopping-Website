package com.shopify.main.service;

import com.shopify.main.entity.Cart;
import com.shopify.main.entity.OrderAddress;
import com.shopify.main.entity.OrderedProductDetails;
import com.shopify.main.entity.Product;
import com.shopify.main.handler.OrderRequest;
import com.shopify.main.repository.CartRepository;
import com.shopify.main.repository.OrderedProductDetailsRepository;
import com.shopify.main.repository.ProductRepository;
import com.shopify.main.utility.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDate;
import java.util.*;

@Service
public class OrderedProductDetailsServiceImpl implements OrderedProductDetailsService{

    @Autowired
    private OrderedProductDetailsRepository productDetailsRepository;

    @Autowired
    private CartRepository cartRepository;


    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    ProductRepository productRepository;

    @Override
    public List<OrderedProductDetails> saveOrderedProduct(OrderRequest orderRequest, Long customerId) {

        List<Cart> cartList=cartRepository.findAllByUserId(customerId);

        if (! ObjectUtils.isEmpty(cartList)){

            List<OrderedProductDetails> productDetailsList = new ArrayList<>();

            Product existingProduct;

            for (Cart cart : cartList){
                OrderedProductDetails productDetails = new OrderedProductDetails();

                productDetails.setProduct(cart.getProduct());
                productDetails.setUser(cart.getUser());
                productDetails.setPrice(cart.getProduct().getDiscountedPrice());
                productDetails.setQuantity(cart.getQuantity());


                existingProduct =cart.getProduct();
                existingProduct.setStock(existingProduct.getStock()-cart.getQuantity());
                productRepository.save(existingProduct);

                productDetails.setPaymentType(orderRequest.getPaymentType());
                productDetails.setStatus("IN PROGRESS");

                productDetails.setOrderId("ORDER_ID_"+UUID.randomUUID().toString());
                productDetails.setOrderDate(LocalDate.now());

                OrderAddress orderAddress = new OrderAddress();

                orderAddress.setAddress(orderRequest.getAddress());
                orderAddress.setCity(orderRequest.getCity());
                orderAddress.setEmail(orderRequest.getEmail());
                orderAddress.setFirstName(orderRequest.getFirstName());
                orderAddress.setLastName(orderRequest.getLastName());
                orderAddress.setPinCode(orderRequest.getPinCode());
                orderAddress.setState(orderRequest.getState());
                orderAddress.setMobileNumber(orderRequest.getMobileNumber());

                productDetails.setOrderAddress(orderAddress);

               // return productDetailsRepository.save(productDetails);
                productDetailsList.add(productDetails);

            }
            return productDetailsRepository.saveAll(productDetailsList);
        }
        return null;
    }

    @Override
    public Page<OrderedProductDetails> getOrderedProductsOfUser(Long userId,Pageable pageable) {

        try{
            return productDetailsRepository.findByUserId(userId,pageable);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public OrderedProductDetails cancelOrder(String orderId) {

        Optional<OrderedProductDetails> productDetailsOptional =productDetailsRepository.findByOrderId(orderId);

        if (productDetailsOptional.isPresent()){
            OrderedProductDetails existingProductDetails=productDetailsOptional.get();
            existingProductDetails.setStatus("CANCELLED");
            return productDetailsRepository.save(existingProductDetails);
        }

        return null;
    }

    @Override
    public Page<OrderedProductDetails> getAllOrders(Pageable pageable) {
        return productDetailsRepository.findAll(pageable);
    }

    @Override
    public boolean updateOrderStatus(String orderId, String status) {

        Optional<OrderedProductDetails> optionalOrderedProductDetails=productDetailsRepository.findByOrderId(orderId);
        if (optionalOrderedProductDetails.isPresent()){
            OrderedProductDetails orderedProductDetails=optionalOrderedProductDetails.get();
            orderedProductDetails.setStatus(status);

            OrderedProductDetails newOrderDetails=productDetailsRepository.save(orderedProductDetails);


            if (!ObjectUtils.isEmpty(newOrderDetails)){
                commonUtils.sendOrderedProductStatusMail(newOrderDetails);
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public OrderedProductDetails getOrderedProductByOrderId(String orderId) {
        Optional<OrderedProductDetails> orderedProductDetails= productDetailsRepository.findByOrderId(orderId);

        if (orderedProductDetails.isPresent()){
            return orderedProductDetails.get();
        }else {
            return null;
        }
    }

    @Override
    public List<OrderedProductDetails> getOrderedProductsByStatus(String status) {
        List<OrderedProductDetails> productDetailsList= productDetailsRepository.findByStatus(status);
        if (!ObjectUtils.isEmpty(productDetailsList)){
            return productDetailsList;
        }
        return null;
    }

    @Override
    public OrderedProductDetails getProductInformationByOrderId(String orderId) {
        OrderedProductDetails productDetails=productDetailsRepository.findByOrderId(orderId).get();
        if (!ObjectUtils.isEmpty(productDetails)){
            return productDetails;
        }
        return null;
    }
}
