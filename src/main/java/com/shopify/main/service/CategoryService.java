package com.shopify.main.service;

import com.shopify.main.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface CategoryService {
    void addNewCategory(Category category, MultipartFile multipartFile);
    boolean checkDataExistOrNotByName(Category category);
    Page<Category> getAllCategoriesByPageable(Pageable pageable);
    List<Category> getAllCategories();
    boolean updateCategory(Category category);
    boolean deleteCategoryById(Long id);
}
