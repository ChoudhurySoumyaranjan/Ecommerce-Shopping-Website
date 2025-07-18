package com.shopify.main.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Product {

    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Please Enter Title")
    private String title;

    @NotBlank(message = "Please Enter Description")
    private String description;

    //@NotEmpty(message = "Please Select Category")
    private String category;

    //@NotBlank(message = "Please Enter Price")
    private Double price;

    //@NotBlank(message = "Please Enter how many stocks You  have")
    private int stock;

    //@NotEmpty(message = "Please Add Image")
    private String image;

    private int discount;

    private Double discountedPrice;
}
