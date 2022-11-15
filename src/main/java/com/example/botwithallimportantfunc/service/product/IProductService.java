package com.example.botwithallimportantfunc.service.product;

import com.example.botwithallimportantfunc.entity.product.Product;
import com.example.botwithallimportantfunc.entity.product.ProductRepr;

import java.util.List;
import java.util.Optional;

public interface IProductService {

    List<ProductRepr> findAll();

    ProductRepr findByProductId(Integer productId);

    void save(ProductRepr productRepr);

    void delete(ProductRepr productRepr);

    boolean contains(ProductRepr productRepr);

    public void update(Product product);

    Optional<ProductRepr> findById(long productId);

    ProductRepr findByAddress(String url);
}
