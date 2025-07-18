package com.shopify.main.repository;

import com.shopify.main.entity.OrderedProductDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderedProductDetailsRepository extends JpaRepository<OrderedProductDetails,Long> {
    List<OrderedProductDetails> findByUserId(Long userId);

    Optional<OrderedProductDetails> findByOrderId(String orderId);

    List<OrderedProductDetails> findByStatus(String status);

    Page<OrderedProductDetails> findByUserId(Long userId, Pageable pageable);
}
