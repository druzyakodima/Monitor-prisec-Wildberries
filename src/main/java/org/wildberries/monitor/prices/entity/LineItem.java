package org.wildberries.monitor.prices.entity;

import org.wildberries.monitor.prices.entity.product.Product;
import org.wildberries.monitor.prices.entity.product.ProductRepr;
import org.wildberries.monitor.prices.entity.user.User;
import org.wildberries.monitor.prices.entity.user.UserRepr;
import lombok.Data;

import javax.persistence.*;
import java.util.Objects;


@Entity
@Table(name = "cart")
@Data
public class LineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(cascade = CascadeType.MERGE)
    private Product product;
    @ManyToOne(cascade = CascadeType.MERGE)
    private User user;

    public LineItem(ProductRepr productRepr, UserRepr userRepr) {
        this.product = new Product(productRepr);
        product.setId(productRepr.getId());
        this.user = new User(userRepr);
        user.setId(userRepr.getId());
    }

    public LineItem(Product product, User user) {
        this.product = product;
        this.user = user;
    }

    public LineItem() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LineItem lineItem = (LineItem) o;
        return product.getId().equals(lineItem.product.getId()) && user.getId().equals(lineItem.user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(product.getId(), user.getId());
    }
}
