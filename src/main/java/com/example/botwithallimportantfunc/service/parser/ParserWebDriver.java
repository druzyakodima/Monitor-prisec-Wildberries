package com.example.botwithallimportantfunc.service.parser;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Data
@PropertySource("/application.properties")
@EnableScheduling
public class ParserWebDriver implements IParser {

    private String title;

    private Integer price;

    private WebDriver webDriver;

    private Integer productId;


    public ParserWebDriver(WebDriver webDriver) {
        System.setProperty("webdriver.chrome.driver", "selenium/chromedriver.exe");
        this.webDriver = webDriver;
    }

    public void setSystemProperty(String key, String value) {
        System.setProperty(key, value);
    }

    public void getData(String url) {

        webDriver.manage().timeouts().implicitlyWait(Duration.ofMillis(600));
        webDriver.get(url);

        data();
    }

    private void data() {

        try {
            price = Integer
                    .parseInt(webDriver.findElement(By.xpath("//ins[@class='price-block__final-price'][1]"))
                            .getText()
                            .replace("₽", "")
                            .trim()
                            .replace(" ", ""));

            productId = Integer.parseInt(webDriver
                    .findElement(By.xpath("//span[@id='productNmId'][1]"))
                    .getText());

            title = webDriver
                    .findElement(By.tagName("h1"))
                    .getText();

        } catch (Exception e) {
            log.warn("В товаре" + webDriver.getCurrentUrl() + " не был найден xpath");

            price = null;

            try {
                title = webDriver
                        .findElement(By.tagName("h1"))
                        .getText();

                productId = Integer.parseInt(webDriver
                        .findElement(By.xpath("//span[@id='productNmId'][1]"))
                        .getText());
            } catch (Exception ex) {

                log.info("Пользователь отправил ссылку на другой сайта");

                title = null;
                productId = null;
            }
        }
    }

    public void quit() {
        webDriver.quit();
    }

    public void close() {
        webDriver.close();
    }

}
