package wildberries_monitor_prices.entity.product;

import lombok.Data;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotEmpty;

@Data
public class ProductRepr {

    private Long id;

    @NotEmpty
    private String address;

    private String title;

    @Nullable
    private Integer price;

    @Nullable
    private Integer productId;

    public ProductRepr(String address, String title, @Nullable Integer price, @Nullable Integer productId) {
        this.address = address;
        this.title = title;
        this.price = price;
        this.productId = productId;
    }

    public ProductRepr(Long id, String address, String title, @Nullable Integer price, @Nullable Integer productId) {
        this.id = id;
        this.address = address;
        this.title = title;
        this.price = price;
        this.productId = productId;
    }

    public ProductRepr(Product product) {
        this.id = product.getId();
        this.address = product.getAddress();
        this.title = product.getTitle();
        this.price = product.getPrice();
        this.productId = product.getProductId();
    }

    public ProductRepr() {
    }
}
