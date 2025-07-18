package com.shopify.main.service;

import com.shopify.main.entity.OrderedProductDetails;
import com.shopify.main.handler.OrderRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderedProductDetailsService {
    List<OrderedProductDetails> saveOrderedProduct(OrderRequest orderRequest, Long customerId);

    Page<OrderedProductDetails> getOrderedProductsOfUser(Long userId,Pageable pageable);

    OrderedProductDetails cancelOrder(String orderId);

    Page<OrderedProductDetails> getAllOrders(Pageable pageable);

    boolean updateOrderStatus(String orderId,String status);
    OrderedProductDetails getOrderedProductByOrderId(String orderId);
    List<OrderedProductDetails> getOrderedProductsByStatus(String status);

    OrderedProductDetails getProductInformationByOrderId(String orderId);

}
