package com.example.botwithallimportantfunc.service.cart;

import com.example.botwithallimportantfunc.entity.LineItem;
import com.example.botwithallimportantfunc.entity.product.ProductRepr;
import com.example.botwithallimportantfunc.entity.user.UserRepr;

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
}
