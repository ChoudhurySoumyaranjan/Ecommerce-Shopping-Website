package com.shopify.main.config;

import com.shopify.main.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


public class CustomUserDetails implements UserDetails{

    private User user;

    public CustomUserDetails(User user){
        this.user=user;
    }
    
    private String phone;
    private String loggedInUserName;
    private Long id;
    private String address;
    private String image;
    private String state;

    public String getImage() {
        return user.getImage();
    }

    public String getState(){
        return user.getState();
    }

    public String getAddress() {
        return user.getAddress();
    }

    public Long getId() {
        return user.getId();
    }


    public String getPhone() {
        return user.getPhone();
    }


    public String getLoggedInUserName() {
        return user.getName();
    }






    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(user.getRole()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {   //email
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getIsEnabled();
    }
}

