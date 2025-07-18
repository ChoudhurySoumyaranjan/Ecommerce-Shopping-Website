package com.shopify.main.repository;

import com.shopify.main.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart,Long> {
    Optional<Cart> findByProductIdAndUserId(Long productId,Long userId);
    //Long countByQuantity(Long user_Id);

    @Query(value = "SELECT SUM(c.quantity) FROM Cart c WHERE c.user.id = :userId",nativeQuery = false)
    Integer sumQuantityByUserId(@Param("userId") Long userId);
    List<Cart> findAllByUserId(Long userId);
}
