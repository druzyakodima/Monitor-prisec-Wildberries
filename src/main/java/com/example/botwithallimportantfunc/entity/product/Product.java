package com.example.botwithallimportantfunc.entity.product;

import com.example.botwithallimportantfunc.entity.LineItem;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(length = 128, unique = true)
    private String address;

    @Column(length = 128)
    private String title;

    @Column(length = 128)
    private Integer price;

    @Column(length = 128)
    private Integer productId;

    @OneToMany(mappedBy = "product")
    private List<LineItem> lineItems;

    public Product(ProductRepr productRepr) {

        this.id = productRepr.getId();
        this.address = productRepr.getAddress();
        this.title = productRepr.getTitle();
        this.price = productRepr.getPrice();
        this.productId = productRepr.getProductId();

    }

    public Product(Long id, String address, String title, Integer price, Integer productId) {
        this.id = id;
        this.address = address;
        this.title = title;
        this.price = price;
        this.productId = productId;
    }

    public Product(String address, String title, Integer price, Integer productId) {
        this.address = address;
        this.title = title;
        this.price = price;
        this.productId = productId;
    }

    public Product() {
    }
}
