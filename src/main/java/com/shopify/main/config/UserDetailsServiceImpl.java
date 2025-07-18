package com.shopify.main.config;

import com.shopify.main.entity.User;
import com.shopify.main.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {


    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> optionalUser =userRepository.findByEmail(username);
        if (optionalUser!=null && optionalUser.isPresent()){
            User tryToLoginUser = optionalUser.get();
            return new CustomUserDetails(tryToLoginUser);
        }else {
            throw  new UsernameNotFoundException("Failed to SignIn");
        }
    }
}
