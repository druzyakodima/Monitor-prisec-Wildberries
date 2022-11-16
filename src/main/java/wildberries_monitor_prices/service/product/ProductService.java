package wildberries_monitor_prices.service.product;


import wildberries_monitor_prices.entity.product.Product;
import wildberries_monitor_prices.entity.product.ProductRepr;
import wildberries_monitor_prices.repositories.ProductRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@Service
public class ProductService implements IProductService {

    private ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductRepr> findAll() {
        return productRepository.findAll()
                .stream()
                .map(ProductRepr::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductRepr findByProductId(Integer productId) {

        List<Product> products = productRepository.findAll();
        ProductRepr productRepr = null;

        for (Product product : products) {
            if (product.getProductId().equals(productId)) {
                productRepr = new ProductRepr(product);
            }
        }
        return productRepr;
    }

    @Transactional
    @Override
    public void save(ProductRepr productRepr) {
        Product product = new Product(productRepr);
        productRepository.save(product);
        if (productRepr.getId() == null) productRepr.setId(product.getId());
    }

    @Override
    public void update(Product product) {
        productRepository.saveAndFlush(product);
    }

    @Transactional
    @Override
    public void delete(ProductRepr productRepr) {
        Product product = new Product(productRepr);
        productRepository.deleteById(product.getId());
    }

    @Override
    public boolean contains(ProductRepr productRepr) {

        List<ProductRepr> products = findAll();

        for (ProductRepr product : products) {
            if (Objects.requireNonNull(product.getProductId()).equals(productRepr.getProductId())) {
                return true;
            }
        }

        return false;
    }

    @Transactional
    @Override
    public Optional<ProductRepr> findById(long productId) {
        return productRepository.findById(productId).map(ProductRepr::new);
    }

    @Transactional
    @Override
    public ProductRepr findByAddress(String url) {

        List<ProductRepr> productReprs = findAll();

        for (ProductRepr productRepr : productReprs) {
            if (productRepr.getAddress().equals(url)) {
                return productRepr;
            }
        }
        return null;
    }


    public ProductService() {
    }
}
