package com.shopify.main.service;

import com.shopify.main.entity.Category;
import com.shopify.main.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService{

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FileService fileService;

    @Value("${aws.s3.bucket.category}")
    private String categoryBucket;

    @Value("${aws.region}")
    private String region;

//    private String UPLOAD_DIR = "src/main/resources/static/courseCategoryImages/";
//    private String imgUrl = "http://localhost:1010/courseCategoryImages/";
    @Override
    public void addNewCategory(Category category, MultipartFile multipartFile) {
        try{
//            String filename=multipartFile.getOriginalFilename();
//            Path filePath =Paths.get(UPLOAD_DIR+filename);
//            Files.write(filePath,multipartFile.getBytes());  //now my image is stored in my project directory


            //code for uploading file on s3 bucket

            Boolean isSuccess=fileService.uploadFileToAwsS3(multipartFile,1);  //bucketType 1 is Category Bucket

            if (isSuccess){

                //https://course-category-images.s3.eu-north-1.amazonaws.com/icecream.avif  , example image url saved on s3 bucket

                String url="https://"+categoryBucket+".s3."+region+".amazonaws.com/"+multipartFile.getOriginalFilename();

                category.setImage(url);
            }



            //category.setImage(imgUrl+filename);
            categoryRepository.save(category);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean checkDataExistOrNotByName(Category category) {
        Optional<Category> optionalCategory=categoryRepository.findByName(category.getName());
        if(optionalCategory.isPresent()){
            return true;
        }else {
            return false;
        }
    }

    @Override
    public Page<Category> getAllCategoriesByPageable(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public boolean updateCategory(Category category) {
        Category savedCategory=categoryRepository.save(category);
        if (savedCategory != null){
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteCategoryById(Long id) {
        try{
            categoryRepository.deleteById(id);
            return  true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
