package com.shopify.main.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@Entity
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class  OrderedProductDetails{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderId;

    private LocalDate orderDate;

    @ManyToOne
    private Product product;

    @ManyToOne
    private User user;

    @OneToOne(cascade = CascadeType.ALL)
    private OrderAddress orderAddress;

    private Double price;

    private String status;

    private int quantity;

    private String paymentType;
}
