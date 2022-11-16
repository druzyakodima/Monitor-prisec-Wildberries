package wildberries_monitor_prices.service.product;

import wildberries_monitor_prices.entity.product.Product;
import wildberries_monitor_prices.entity.product.ProductRepr;

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
