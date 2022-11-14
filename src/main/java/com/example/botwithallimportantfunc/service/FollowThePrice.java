package com.example.botwithallimportantfunc.service;

import com.example.botwithallimportantfunc.entity.LineItem;
import com.example.botwithallimportantfunc.model.TelegramBot;
import com.example.botwithallimportantfunc.service.cart.ICartService;
import com.example.botwithallimportantfunc.service.parser.ParserWebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FollowThePrice {

    private ICartService cartService;

    @Autowired
    public FollowThePrice(ICartService cartService) {
        this.cartService = cartService;
    }

    public void followThePrice(TelegramBot telegramBot, ChromeOptions options) {
        
        ParserWebDriver parser = new ParserWebDriver(new ChromeDriver(options));

        List<LineItem> products = cartService.findAll();
        StringBuilder builder = new StringBuilder();

        for (LineItem product : products) {

            parser.getData(product.getProduct().getAddress());

            Integer price = parser.getPrice();
            Integer oldPrice = product.getProduct().getPrice();

            if (!price.equals(oldPrice)) {

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
        }
        //parser.quit();
        parser.close();
    }
}
