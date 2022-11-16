package org.wildberries.monitor.prices.service.cart;

import org.wildberries.monitor.prices.entity.LineItem;
import org.wildberries.monitor.prices.entity.product.ProductRepr;
import org.wildberries.monitor.prices.entity.user.User;
import org.wildberries.monitor.prices.entity.user.UserRepr;

import java.util.List;
import java.util.Set;

public interface ICartService {

    void save(ProductRepr productRepr, UserRepr userRepr);

    boolean remove(long chatId, String url);

    void update(LineItem lineItem);
    boolean contains(long chatId, Integer productId);

    void removeAllForUser(long userId);

    Set<LineItem> findAllItemsForUser(long userId);

    ProductRepr findByTitle(String title);

    ProductRepr findById(long userId, Integer productId);

    public List<LineItem> findAll();

    public boolean isEmptyCart(long chatId);

    public Set<User> buyers();
}
