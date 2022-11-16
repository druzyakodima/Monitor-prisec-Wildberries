package org.wildberries.monitor.prices.service;

import org.wildberries.monitor.prices.entity.LineItem;
import org.wildberries.monitor.prices.controller.TelegramBot;
import org.wildberries.monitor.prices.service.cart.ICartService;
import org.wildberries.monitor.prices.service.parser.ParserWebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FollowThePrice {

    private ICartService cartService;

    private ParserWebDriver parser;

    @Autowired
    public FollowThePrice(ICartService cartService, ParserWebDriver parser) {
        this.parser = parser;
        this.cartService = cartService;
    }

    public void followThePrice(TelegramBot telegramBot) {

        List<LineItem> products = cartService.findAll();
        StringBuilder builder = new StringBuilder();

        for (LineItem product : products) {

            parser.getData(product.getProduct().getAddress());

            Integer price = parser.getPrice();
            Integer oldPrice = product.getProduct().getPrice();

            long chatId = product.getUser().getChatId();
            String url = product.getProduct().getAddress();
            String title = parser.getTitle();

            if (telegramBot.checkOutOfStock(price, chatId, url, title)) {
                cartService.remove(chatId, url);
                continue;
            }


            if (oldPrice > price) {
                telegramBot.changePriceAndNotificationUsers(builder, product, price, url, title, oldPrice);
            }

        }
        //parser.quit();
        //parser.close();
    }
}