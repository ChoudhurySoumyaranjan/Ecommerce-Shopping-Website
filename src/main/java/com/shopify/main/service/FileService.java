package com.shopify.main.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    public Boolean uploadFileToAwsS3(MultipartFile multipartFile,Integer bucketType);
}
