package wildberries_monitor_prices.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@Data
@Slf4j
@EnableScheduling
@PropertySource("/application.properties")
@Configuration
public class BotConfig {

    @Value("${bot.username}")
    private String username;

    @Value("${bot.token}")
    private String token;

    public BotConfig() {
    }
}
