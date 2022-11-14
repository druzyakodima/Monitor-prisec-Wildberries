package com.example.botwithallimportantfunc.model;

import com.example.botwithallimportantfunc.config.BotConfig;
import com.example.botwithallimportantfunc.entity.LineItem;
import com.example.botwithallimportantfunc.entity.product.Product;
import com.example.botwithallimportantfunc.entity.product.ProductRepr;
import com.example.botwithallimportantfunc.entity.user.User;
import com.example.botwithallimportantfunc.entity.user.UserRepr;
import com.example.botwithallimportantfunc.service.FollowThePrice;
import com.example.botwithallimportantfunc.service.cart.CartService;
import com.example.botwithallimportantfunc.service.parser.ParserWebDriver;
import com.example.botwithallimportantfunc.service.product.ProductService;
import com.example.botwithallimportantfunc.service.user.UserService;
import com.vdurmont.emoji.EmojiParser;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Getter
@Setter
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private BotConfig config;
    private ProductService productService;

    private ParserWebDriver parser;
    private UserService userService;

    private FollowThePrice followThePrice;

    private ChromeOptions options;
    private CartService cartService;
    private final String ERROR = "Error occurred: ";
    private String IS_EMPTY_CART = "❌ Пустая корзина";

    {
        options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("window-size=1200x600");

        try {
            String binaryPath = EnvironmentUtils.getProcEnvironment().get("GOOGLE_CHROME_SHIM");
            options.setBinary(binaryPath);
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        parser = new ParserWebDriver(new ChromeDriver(options));
    }

    public TelegramBot() {
    }

    @Autowired
    public TelegramBot(BotConfig config,
                       ProductService productService,
                       UserService userService,
                       CartService cartService,
                       FollowThePrice followThePrice) {

        this.config = config;
        this.productService = productService;
        this.userService = userService;
        this.cartService = cartService;
        this.followThePrice = followThePrice;
    }

    @Override
    public String getBotUsername() {
        return config.getUsername();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        Thread t = new Thread(() -> telegramCommand(update));
        t.setDaemon(true);
        t.start();
    }

    private void telegramCommand(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String message = update.getMessage().getText();

            Pattern matches = Pattern.compile("h.+?p");
            Matcher matcher = matches.matcher(message);

            if (checkAddressOrProductId(chatId, message, matcher)) {
                return;
            }

            switch (message) {
                case "/start" -> startCommand(chatId, update.getMessage());
                case "❤ Помощь" -> helpCommand(chatId);
                case "\uD83D\uDD75 Следить за ценами товаров" -> getHeadLine(chatId);
                case "\uD83D\uDED2 Корзина" -> getListProducts(chatId);
                case "\uD83D\uDD22 Поиск товара по артикулу" -> findProduct(chatId);
                case "🗑 Удалить товар" -> removeProduct(chatId);
                case "\uD83E\uDDE8 Очистить корзину" -> removeAllProductInCartUser(chatId);
                default -> {
                    SendMessage sendMessage = getSendMessage(chatId, "Такой команды нет!");
                    executeMessage(sendMessage);
                    log.info("Пользователь " + userService.findByChatId(chatId).getUserName() + " написал не правильный запрос");
                }
            }

        }
        callBackQuery(update);
    }

    public boolean checkAddressOrProductId(long chatId, String message, Matcher matcher) {
        if (matcher.find()) {
            if (!saveProductForDB(chatId, message.substring(message.indexOf("h")))) {
                SendMessage sendMessage = getSendMessageWithParser(chatId, "Ой, а точно такой товар есть на <a href=\"https://www.wildberries.ru/\">WILDBERRIES</a>? \n" +
                        "Или попробуй ещё раз добавить!");
                executeMessage(sendMessage);
            }
            return true;

        } else if (message.matches("[-+]?\\d+")) {
            findProductByProductId(chatId, Integer.valueOf(message));
            return true;
        }
        return false;
    }

    public void callBackQuery(Update update) {
        if (update.hasCallbackQuery()) {

            String callbackData = update.getCallbackQuery().getData();
            long idChat = update.getCallbackQuery().getMessage().getChatId();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            String username = update.getCallbackQuery().getMessage().getChat().getUserName();

            ProductRepr productRepr = cartService.findById(idChat, Integer.parseInt(callbackData));

            if (productRepr != null) {
                if (cartService.remove(idChat, productRepr.getAddress())) {
                    String text = "Товар " + productRepr.getTitle() + " удален";
                    executeEditMessageText(idChat, messageId, text);
                    log.info("Пользовватель удалил " + productRepr.getTitle() + "из корзины" + username);

                }
            }
        }
    }

    public void removeAllProductInCartUser(long chatId) {

        if (!cartService.isEmptyCart(chatId)) {
            cartService.removeAllForUser(chatId);

            SendMessage message = getSendMessageWithParser(chatId, "\uD83D\uDCA5 Вы очистили корзину");
            executeMessage(message);

        } else {
            SendMessage sendMessage = getSendMessageWithParser(chatId, IS_EMPTY_CART);
            executeMessage(sendMessage);
        }
    }

    public void executeEditMessageText(long idChat, long messageId, String text) {
        EditMessageText messageText = new EditMessageText();
        messageText.setChatId(idChat);
        messageText.setText(text);
        messageText.setMessageId(Math.toIntExact(messageId));

        try {
            execute(messageText);
        } catch (TelegramApiException e) {
            log.error(ERROR + e.getMessage());
        }
    }


    public void removeProduct(long chatId) {

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        Set<LineItem> products = cartService.findAllItemsForUser(chatId);

        if (cartService.isEmptyCart(chatId)) {
            SendMessage sendMessage = getSendMessageWithParser(chatId, IS_EMPTY_CART);
            executeMessage(sendMessage);
            return;
        }

        for (LineItem product : products) {
            buttons.add(Arrays.asList(InlineKeyboardButton
                    .builder()
                    .text(product.getProduct().getTitle())
                    .callbackData(
                            String.valueOf(
                                    product.getProduct()
                                            .getProductId()
                            )
                    ).build()));
        }
        try {
            execute(SendMessage.builder()
                    .text("Какой товар хочешь удалить?")
                    .chatId(chatId)
                    .replyMarkup(
                            InlineKeyboardMarkup.
                                    builder()
                                    .keyboard(buttons)
                                    .build()
                    )
                    .build());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void findProductByProductId(long chatId, Integer productId) {

        ProductRepr productRepr = cartService.findById(chatId, productId);

        if (productRepr == null) {
            SendMessage sendMessage = getSendMessageWithParser(chatId, "В вашей корзине нет такого товара");
            executeMessage(sendMessage);
            return;
        }

        SendMessage sendMessage = getSendMessageWithParser(chatId, "\uD83D\uDD3A" + "<a href=\"" + productRepr.getAddress() + "\">"
                + productRepr.getTitle() + "</a> \n" + "⬆ " + productRepr.getPrice());

        executeMessage(sendMessage);
        log.info("Пользователь " + userService.findByChatId(chatId).getUserName() + " искал " + productRepr.getTitle());
    }


    public void getListProducts(long chatId) {

        StringBuilder builder = new StringBuilder();
        Set<LineItem> products = cartService.findAllItemsForUser(chatId);

        if (cartService.isEmptyCart(chatId)) {
            SendMessage sendMessage = getSendMessageWithParser(chatId, IS_EMPTY_CART);
            executeMessage(sendMessage);
            return;
        }


        for (LineItem product : products) {
            builder
                    .append("\uD83D\uDD3A")
                    .append("<a href=\"")
                    .append(product.getProduct().getAddress())
                    .append("\">").append(product.getProduct().getTitle())
                    .append("</a> \n")
                    .append("⬆ ")
                    .append(product.getProduct().getPrice()).append(" ₽")
                    .append("\n\n");
        }

        SendMessage sendMessage = getSendMessageWithParser(chatId, builder.toString().trim());
        executeMessage(sendMessage);
        log.info("Пользователь " + userService.findByChatId(chatId).getUserName() + " просматривал корзину");
    }

    public void findProduct(long chatId) {
        SendMessage sendMessage = getSendMessageWithParser(chatId, "Введите артикул товара");
        executeMessage(sendMessage);
    }

    public void getHeadLine(long chatId) {
        String messageForUser = "Введите сслыку на товар, чтобы следить за ценой";
        SendMessage message = getSendMessage(chatId, messageForUser);
        executeMessage(message);
    }

    public boolean saveProductForDB(long chatId, String url) {

        if (checkDB(chatId, url)) {
            return true;
        }

        parser.getData(url);

        Integer price = parser.getPrice();

        String title = parser.getTitle();

        Integer productId = parser.getProductId();


        if (checkOutOfStock(price, chatId, url, title)) return true;

        if (price == null || title == null || productId == null) {
            return false;
        }


        ProductRepr productRepr = new ProductRepr(url, title, price, productId);
        UserRepr userRepr = userService.findByChatId(chatId);

        SendMessage sendMessage;

        if (!productService.contains(productRepr)) {
            productService.save(productRepr);
            cartService.save(productService.findByProductId(productId), userRepr);

            sendMessage = getSendMessageWithParser(chatId, "Теперь я слежу за этим товаром\uD83D\uDD0D" + "\n" + title + " \n ➡ " + price + " ₽");
        } else {
            sendMessage = getSendMessage(chatId, "Этот товар уже в работе");
        }

        executeMessage(sendMessage);
        log.info("Пользователь " + userRepr.getUserName() + " добавил" + productRepr.getTitle());
        return true;
    }

    private boolean checkDB(long chatId, String url) {

        if (productService.findByAddress(url) == null) {
            return false;
        }

        Product product = new Product(productService.findByAddress(url));
        UserRepr userRepr = userService.findByChatId(chatId);

        SendMessage sendMessage;

        if (!cartService.contains(chatId, product.getProductId())) {

            cartService.save(new ProductRepr(product), userRepr);

            sendMessage = getSendMessageWithParser(chatId, "Теперь я слежу за этим товаром\uD83D\uDD0D" + "\n"
                    + product.getTitle() +
                    " \n ➡ " + product.getPrice() + " ₽");
        } else {
            sendMessage = getSendMessage(chatId, "Этот товар уже в работе");
        }

        executeMessage(sendMessage);
        return true;
    }


    public void helpCommand(long chatId) {
        SendMessage sendMessage = getSendMessageWithParser(chatId, """
                Не знаешь как пользоваться ботом?

                Не переживай, просто нажми\uD83D\uDC46 на кнопку "\uD83D\uDD75 Следить за ценами товаров"".
                После этого товар добавится в мой список и я буду следить за ним.
                И когда он станет дешевле, я сразу напишу тебе!\uD83D\uDCF2
                А ещё у меня есть корзина, ты можем посмотреть товары которые есть в списке.
                Если товар стал тебе не интересен, ты можешь удалить его\uD83D\uDEAE. Просто нажми на кнопку "удалить товар" и выбери не нужный.""");
        executeMessage(sendMessage);
    }

    public void startCommand(long chatId, Message message) {

        if (userService.findByChatId(chatId) == null) {
            registerUser(message);
        }

        String answerForStart = EmojiParser.parseToUnicode("""
                \uD83D\uDE4F Чем я могу помочь?\s

                Отправь мне любой товар из Wildberries и я буду следить\uD83D\uDD75 за ним.

                И 📲сообщу когда товар станет дешевле""");

        SendMessage sendMessage = getSendMessage(chatId, answerForStart);

        String text = "Кстати, полный список акционных товаров \uD83D\uDCD2 " + "<a href=\"https://www.wildberries.ru/promotions\">Акции на Wildberries</a>";

        SendMessage secondSendMessage = getSendMessageWithParser(chatId, text);

        ReplyKeyboardMarkup keyboardMarkup = getKeyBoardMarkup();
        secondSendMessage.setReplyMarkup(keyboardMarkup);

        executeMessage(sendMessage);
        executeMessage(secondSendMessage);
        log.info("Присоеденился пользователь с ником " + userService.findByChatId(chatId).getUserName());
    }

    private void registerUser(Message message) {

        if (userService.findByChatId(message.getChatId()) == null) {

            var chatId = message.getChatId();
            var chat = message.getChat();

            UserRepr userRepr = new UserRepr(chatId, chat.getFirstName(), chat.getLastName(), chat.getUserName());

            userService.save(userRepr);
            log.info("Новый пользователь " + userRepr.getUserName());
        }

    }

    private ReplyKeyboardMarkup getKeyBoardMarkup() {

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();

        row.add(EmojiParser.parseToUnicode("\uD83D\uDD75 Следить за ценами товаров"));
        row.add("\uD83D\uDED2 Корзина");

        keyboardRows.add(row);
        row = new KeyboardRow();

        row.add(EmojiParser.parseToUnicode("\uD83D\uDDD1 Удалить товар"));
        row.add("\uD83E\uDDE8 Очистить корзину");
        row.add("❤ Помощь");

        keyboardRows.add(row);
        keyboardMarkup.setKeyboard(keyboardRows);

        return keyboardMarkup;
    }

    public static SendMessage getSendMessageWithParser(long chatId, String text) {
        String secondAnswer = EmojiParser.parseToUnicode(text);
        SendMessage secondSendMessage = getSendMessage(chatId, secondAnswer);
        secondSendMessage.setParseMode("HTML");
        secondSendMessage.disableWebPagePreview();
        return secondSendMessage;
    }

    public static SendMessage getSendMessage(long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text);
        sendMessage.setChatId(chatId);
        return sendMessage;

    }

    public void executeMessage(SendMessage message) {

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR + e.getMessage());
        }
    }


    @Scheduled(cron = "${cron.scedulerEveryMinute}")
    public void followThePrice() {
        followThePrice.followThePrice(this, options);
    }

    public boolean checkOutOfStock(Integer price, long chatId, String url, String title) {
        if (price == null && url != null && title != null) {
            SendMessage productIsOver = getSendMessageWithParser(chatId, "Товара " + "<a href=\"" + url + "\">" + title + "</a> нет в наличии");
            executeMessage(productIsOver);
            // добавить таблицу в бд с отложеными товарами
            return true;
        }
        return false;
    }

    public void changePriceAndNotificationUsers(StringBuilder builder,
                                                LineItem product,
                                                Integer price,
                                                String url,
                                                String title,
                                                Integer oldPrice) {


        log.info("Товар " + "<a href=\"" + url + "\">" + title + "</a>" + " стал дешевле\n" + "Старая стоимость " + oldPrice + "Новая стоимость" + price);

        builder
                .append("\uD83D\uDD3A")
                .append("<a href=\"")
                .append(product.getProduct().getAddress())
                .append("\">")
                .append(product.getProduct().getTitle())
                .append("</a> \n").append("⬆ ")
                .append(product.getProduct().getPrice())
                .append("₽");

        Set<User> users = cartService.buyers();

        for (User user : users) {

            long chatId = user.getChatId();

            if (cartService.findAllItemsForUser(chatId).contains(product)) {

                String username = user.getFirstName() == null ? user.getUserName() : user.getFirstName();

                SendMessage message = getSendMessageWithParser(user.getChatId(), username + " стоимость " + builder + "изменилась " + price + "₽");
                executeMessage(message);

                Product updateProduct = product.getProduct();
                updateProduct.setPrice(price);

                LineItem lineItem = new LineItem(updateProduct, user);
                cartService.update(lineItem);
                productService.update(updateProduct);

                builder.delete(0, builder.length() - 1);
            }
        }

    }
}