package com.shopify.main.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Product product;

    @ManyToOne
    private  User user;

    private int quantity;

    @Transient
    private double price;

    @Transient
    private double totalPrice;

//    @Transient
//    private Double subTotalPrice;

}
