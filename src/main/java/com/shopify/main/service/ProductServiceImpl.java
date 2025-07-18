package com.shopify.main.service;

import com.shopify.main.entity.Category;
import com.shopify.main.entity.Product;
import com.shopify.main.repository.ProductRepository;
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
public class ProductServiceImpl  implements ProductService{

    @Autowired
    private ProductRepository productRepository;

    @Value("${aws.s3.bucket.product}")
    private String productBucket;

    @Value("${aws.region}")
    private String region;

    @Autowired
    private FileService fileService;

    @Override
    public boolean addProduct(Product product, MultipartFile multipartFile) {

        //code for storing file in system
//        String UPLOAD_DIR = "src/main/resources/static/productImages/";
//        String url = "http://localhost:1010/productImages/";
        try {

//            String fileName = multipartFile.getOriginalFilename();
//            Path filePath = Paths.get(UPLOAD_DIR + fileName);
//            Files.write(filePath, multipartFile.getBytes());
//
//            product.setImage(url+fileName);

            //code for uploading file on s3 bucket

            Boolean isSuccess=fileService.uploadFileToAwsS3(multipartFile,2);  //bucketType 2 is Product Bucket

            if (isSuccess){

                //https://shopifyy-product-image.s3.eu-north-1.amazonaws.com/motherdairy.webp  , example image url saved on s3 bucket

                String url="https://"+productBucket+".s3."+region+".amazonaws.com/"+multipartFile.getOriginalFilename();

                product.setImage(url);
            }



            product.setDiscount(0);
            Double discountedAmount = product.getPrice()*(product.getDiscount()/100.00);
            product.setDiscountedPrice(product.getPrice()-discountedAmount);
            Product p =productRepository.save(product);
            if (p!=null){
                return true;
            }
            return false;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Page<Product> getAllProductsByPageable(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> findProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Optional<Product> getProductByTitle(String title) {
        return productRepository.findByTitle(title);
    }

    @Override
    public boolean deleteProduct(Long id) {
        try{
            productRepository.deleteById(id);
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Product> getProductsByCategoryName(String category) {
        return productRepository.findByCategory(category);
    }

    @Override
    public List<Product> getProductsBySearch(String keyword) {
        return productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(keyword,keyword);
    }

//    @Override
//    public Product getProductDetails(Long productId) {
//        Optional<Product> optionalProduct=productRepository.findById(productId);
//        if (optionalProduct.isPresent()){
//            return optionalProduct.get();
//        }
//        return null;
//    }
}
