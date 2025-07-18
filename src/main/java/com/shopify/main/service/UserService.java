package com.shopify.main.service;

import com.shopify.main.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User saveUserDetails(User user, MultipartFile multipartFile) throws Exception;
    Page<User> getAllUserInfo(Pageable pageable);
    boolean blockUserFromLogin(Long id);
    boolean unBlockUser(Long id);
    Optional<User> findUserByEmail(String email);
    User setRestTokenForForgottenPassword(String email,String resetToken);
    User resetPasswordThroughToken(String token,String password);

    Optional<User> getUserInfo(Long id);

    User updateUserPersonalInfo(User newUserInfo,MultipartFile multipartFile) throws IOException;

    boolean changeAccountPassword(String currentPassword,
                                  String confirmedPassword,
                                  Long uid);
    User addNewAdmin(User user,MultipartFile multipartFile);

    Page<User> getAllAdmin(Pageable pageable);

    Page<User> getAdminBySearchedName(String name,Pageable pageable);

    Page<User> getCustomersBySearching(String name,Pageable pageable);

}
