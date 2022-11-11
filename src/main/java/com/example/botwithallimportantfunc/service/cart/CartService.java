package com.example.botwithallimportantfunc.service.cart;

import com.example.botwithallimportantfunc.entity.LineItem;
import com.example.botwithallimportantfunc.entity.product.ProductRepr;
import com.example.botwithallimportantfunc.entity.user.User;
import com.example.botwithallimportantfunc.entity.user.UserRepr;
import com.example.botwithallimportantfunc.repositories.ICartRepository;
import com.example.botwithallimportantfunc.service.product.IProductService;
import com.example.botwithallimportantfunc.service.user.IUserService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Data
@Service
public class CartService implements ICartService {

    private IProductService productService;

    private IUserService userService;

    private ICartRepository cartRepository;

    @Autowired
    public CartService(IProductService productService, IUserService userService, ICartRepository cartRepository) {
        this.productService = productService;
        this.userService = userService;
        this.cartRepository = cartRepository;
    }

    public CartService() {
    }

    @Transactional
    @Override
    public void save(ProductRepr productRepr, UserRepr userRepr) {
         cartRepository.saveAndFlush(new LineItem(productRepr, userRepr));
    }

    @Transactional
    @Override
    public void update(LineItem lineItem) {
        cartRepository.saveAndFlush(lineItem);
    }

    public List<LineItem> findAll() {
        return cartRepository.findAll();
    }

    @Transactional
    @Override
    public boolean remove(long chatId, String url) {
        UserRepr userRepr = userService.findByChatId(chatId);
        ProductRepr productRepr = productService.findByAddress(url);

        if (userRepr == null || productRepr == null) {
            return false;
        }

        Set<LineItem> lineItem = findAllItemsForUser(chatId);

        for (LineItem item : lineItem) {
            if (item.getProduct().getAddress().equals(url)) {
                cartRepository.deleteById(item.getId());
                return true;
            }
        }
        return false;
    }


    @Transactional
    @Override
    public void removeAllForUser(long chatId) {

        List<LineItem> itemList = cartRepository.findAll();

        for (LineItem lineItem : itemList) {
            if (lineItem.getUser().getChatId() == chatId) {
                cartRepository.delete(lineItem);
            }
        }
    }

    @Override
    public Set<LineItem> findAllItemsForUser(long chatId) {

        List<LineItem> list = cartRepository.findAll();
        Set<LineItem> itemListForUser = new LinkedHashSet<>();

        for (LineItem item : list) {
            if (item.getUser().getChatId() == chatId) {
                itemListForUser.add(item);
            }
        }

        return itemListForUser;
    }

    @Override
    public ProductRepr findByTitle(String title) {
        List<LineItem> listItem = cartRepository.findAll();

        if (listItem.size() == 0) {
            log.info("Нет товаров в корзине");
            return null;
        }
        for (LineItem lineItem : listItem) {
            if (lineItem.getProduct().getTitle().equals(title)) {
                return new ProductRepr(lineItem.getProduct());
            }
        }
        return null;
    }

    @Transactional
    @Override
    public ProductRepr findById(long chatId, Integer productId) {

        List<LineItem> listItem = cartRepository.findAll();

        if (listItem.size() == 0) {
            log.info("Нет товаров в корзине");
            return null;
        }

        for (LineItem lineItem : listItem) {
            if (lineItem.getProduct().getProductId().equals(productId)) {
                return new ProductRepr(lineItem.getProduct());
            }
        }

        return null;
    }

    public Set<User> buyers() {

        Set<User> users = new LinkedHashSet<>();
        List<LineItem> carts = findAll();

        carts.forEach(c -> users.add(c.getUser()));

        return users;
    }

    @Override
    public boolean contains(long chatId, Integer productId) {
        Set<LineItem> productsForUser = findAllItemsForUser(chatId);

        for (LineItem lineItem : productsForUser) {
            if (lineItem.getProduct().getProductId().equals(productId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmptyCart(long chatId) {
        return findAllItemsForUser(chatId).size() == 0;
    }
}
