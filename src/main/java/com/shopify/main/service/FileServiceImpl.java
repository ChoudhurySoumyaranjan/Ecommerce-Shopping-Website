package com.shopify.main.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class FileServiceImpl implements FileService{

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucket.category}")
    private String categoryBucket;

    @Value("${aws.s3.bucket.product}")
    private String productBucket;

    @Value("${aws.s3.bucket.profile}")
    private String profileBucket;

    @Override
    public Boolean uploadFileToAwsS3(MultipartFile multipartFile,Integer bucketType) {

        try {
            String bucketName;

            if (bucketType==1){
                bucketName=categoryBucket;
            } else if (bucketType==2) {
                bucketName=productBucket;
            }else {
                bucketName=profileBucket;
            }

            String filename=multipartFile.getOriginalFilename();

            InputStream fileInputStream =multipartFile.getInputStream();

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(multipartFile.getContentType());
            objectMetadata.setContentLength(multipartFile.getSize());

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName,filename,fileInputStream,objectMetadata);

            PutObjectResult savedData =amazonS3.putObject(putObjectRequest);

            if (savedData!=null){
                return true;
            }else {
                return false;
            }

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
