package com.shopify.main.repository;

import com.shopify.main.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Long> {
    String mQuery="select * from category where name= :name";
    @Query(value = mQuery,nativeQuery = true)
    Optional<Category> findByName(@Param("name")String name);

    @Query(value = "select * from category where id = :id",nativeQuery = true)
    Optional<Category> findCategoryById(@Param("id")Long id);
}
