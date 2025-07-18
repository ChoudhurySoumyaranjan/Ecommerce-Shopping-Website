package com.shopify.main.service;

import com.shopify.main.entity.User;
import com.shopify.main.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.s3.bucket.profile}")
    private String profileBucket;

    @Autowired
    private FileService fileService;

    @Override
    public User saveUserDetails(User user, MultipartFile multipartFile) throws Exception {

//        String UPLOAD_DIR = "src/main/resources/static/userProfilePic/";
//        String URL = "http://localhost:1010/userProfilePic/";

        if (multipartFile.isEmpty()){
            user.setImage("https://shopifyy-profile-image.s3.eu-north-1.amazonaws.com/"+"defaultProfile.jpg");
        }else {

            //code for storing image on project directory
//            String fileName=multipartFile.getOriginalFilename();
//            Path filePath =Paths.get(
//                    UPLOAD_DIR+fileName
//            );
//
//            Files.write(filePath,multipartFile.getBytes());
//            user.setImage(URL+fileName);

            //code for uploading file on s3 bucket
            Boolean isSuccess=fileService.uploadFileToAwsS3(multipartFile,3);  //bucketType 3 is Profile Bucket

            if (isSuccess){

                ////https://shopifyy-profile-image.s3.eu-north-1.amazonaws.com/soumya.jpg  // example image url saved on s3 bucket

                String url="https://"+profileBucket+".s3."+region+".amazonaws.com/"+multipartFile.getOriginalFilename();

                user.setImage(url);
            }
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setIsEnabled(true);  //by default account is unblocked

        user.setRole("ROLE_USER");  //by default set role to ROLE_USER

        return userRepository.save(user);
    }

    @Override
    public Page<User> getAllUserInfo(Pageable pageable) {

        return userRepository.findByRole("ROLE_USER",pageable);
    }

    @Override
    public boolean blockUserFromLogin(Long id) {
        Optional<User> optionalUser =userRepository.findById(id);
        if (optionalUser.isPresent()){

            User user=optionalUser.get();
            user.setIsEnabled(false);

            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public boolean unBlockUser(Long id) {
        Optional<User> optionalUser=userRepository.findById(id);
        if (optionalUser.isPresent()){
            User user=optionalUser.get();
            user.setIsEnabled(true);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User setRestTokenForForgottenPassword(String email, String resetToken) {
        Optional<User> optionalUser=userRepository.findByEmail(email);
        if (optionalUser.isPresent()){
            User user=optionalUser.get();
            user.setResetToken(resetToken);
           return userRepository.save(user);
        }
        return null;
    }

    @Override
    public User resetPasswordThroughToken(String token, String password) {
        Optional<User> optionalUser=userRepository.findByResetToken(token);
        if (optionalUser.isPresent()){
            User user=optionalUser.get();
            user.setPassword(passwordEncoder.encode(password));
            user.setResetToken(null);
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public Optional<User> getUserInfo(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User updateUserPersonalInfo(User newUserInfo, MultipartFile multipartFile) throws IOException {
        User existingUser=userRepository.findById(newUserInfo.getId()).get();

        existingUser.setName(newUserInfo.getName());
        existingUser.setEmail(newUserInfo.getEmail());
        existingUser.setPhone(newUserInfo.getPhone());

        if (!multipartFile.isEmpty()){

//            String URL="http://localhost:1010/userProfilePic/";
//            String LOCAL_DIR = "src/main/resources/static/userProfilePic/";
//
//            String filename=multipartFile.getOriginalFilename();
//            Path filePath=Paths.get(LOCAL_DIR+filename);
//            Files.write(filePath,multipartFile.getBytes());
//
//            existingUser.setImage(URL+filename);

            //code for uploading file on s3 bucket
            Boolean isSuccess=fileService.uploadFileToAwsS3(multipartFile,3);  //bucketType 3 is Profile Bucket

            if (isSuccess){

                ////https://shopifyy-profile-image.s3.eu-north-1.amazonaws.com/motherdairy.webp  , example image url saved on s3 bucket

                String url="https://"+profileBucket+".s3."+region+".amazonaws.com/"+multipartFile.getOriginalFilename();

                existingUser.setImage(url);
            }
        }

        return userRepository.save(existingUser);
    }

    @Override
    public boolean changeAccountPassword(String currentPassword, String confirmedPassword,Long uid) {

        User user=userRepository.findById(uid).get();
        if(passwordEncoder.matches(currentPassword,user.getPassword())){
            user.setPassword(passwordEncoder.encode(confirmedPassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public User addNewAdmin(User user,MultipartFile multipartFile) {

        //code for storing file on project
//        String URL = "http://localhost:1010/adminProfilePic/";
//
//        String DIR = "src/main/resources/static/adminProfilePic/";

        try {

            if (!ObjectUtils.isEmpty(multipartFile)){

                //code for storing file on project directory
//                String fileName = multipartFile.getOriginalFilename();
//                Path filePath = Paths.get(DIR+fileName);
//                Files.write(filePath,multipartFile.getBytes());
//                user.setImage(URL+fileName);

                //code for uploading file on s3 bucket
                Boolean isSuccess=fileService.uploadFileToAwsS3(multipartFile,3);  //bucketType 3 is Profile Bucket

                if (isSuccess){

                    //https://shopifyy-profile-image.s3.eu-north-1.amazonaws.com/motherdairy.webp  , example image url saved on s3 bucket

                    String url="https://"+profileBucket+".s3."+region+".amazonaws.com/"+multipartFile.getOriginalFilename();

                    user.setImage(url);
                }

            }else {

                //https://shopifyy-profile-image.s3.eu-north-1.amazonaws.com/defaultProfile.jpg
                user.setImage("https://"+profileBucket+".s3."+region+".amazonaws.com/defaultProfile.jpg");
            }

            user.setRole("ROLE_ADMIN");
            user.setIsEnabled(true);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            return userRepository.save(user);

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Page<User> getAllAdmin(Pageable pageable) {

        return userRepository.findByRole("ROLE_ADMIN",pageable);
    }

    @Override
    public Page<User> getAdminBySearchedName(String name, Pageable pageable) {
        return userRepository.findByNameContainingIgnoreCaseAndRole(name,"ROLE_ADMIN",pageable);
    }

    @Override
    public Page<User> getCustomersBySearching(String name, Pageable pageable) {
        return userRepository.findByNameContainingIgnoreCaseAndRole(name,"ROLE_USER",pageable);
    }
}
