package com.shopify.main.repository;

import com.shopify.main.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByResetToken(String resetToken);
    //@Query("SELECT * FROM user WHERE r.role = :role")
    Page<User> findByRole(String role, Pageable pageable);

    Page<User> findByNameContainingIgnoreCaseAndRole(String nameFragment, String role,Pageable pageable);
}
