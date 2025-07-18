package com.shopify.main.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Entity

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Pattern(regexp = "^[a-z,A-Z ]{5,25}$", message = "Please Enter Valid Name ")
    private String name;

    @Pattern( regexp = "^.{6,}$",
            message = "Please Enter Valid Password")
    private String password;

    @Pattern(regexp = "^.{6,}$", message = "Please Enter Your Address")
    private String address;

    @Pattern(regexp = ".*\\S.*",message = "Please Enter Your State")
    private String state;

    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$",
            message = "Invalid email format")
    private String email;

    @Pattern( regexp = "^[0-9]{10}$",
            message = "Invalid phone number format")
    private String phone;

    private String image;

    private Boolean isEnabled;

    private String role;  // e.g. ROLE_USER, ROLE_ADMIN

    private  String resetToken;
}
