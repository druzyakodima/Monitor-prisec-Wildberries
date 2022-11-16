package org.wildberries.monitor.prices.service.product;

import org.wildberries.monitor.prices.entity.product.Product;
import org.wildberries.monitor.prices.entity.product.ProductRepr;

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
