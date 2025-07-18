package com.shopify.main.service;

import com.shopify.main.entity.Category;
import com.shopify.main.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    boolean addProduct(Product product, MultipartFile multipartFile);
    Page<Product> getAllProductsByPageable(Pageable pageable);
    List<Product> getAllProducts();
    Optional<Product> findProductById(Long id);
    Optional<Product> getProductByTitle(String title);
    boolean deleteProduct(Long id);

    List<Product> getProductsByCategoryName(String category);

//    Product getProductDetails(Long productId);

    List<Product> getProductsBySearch(String keyword);
}
