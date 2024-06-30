package com.ua.osa.tradingbot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by S.Orlov on 01.05.23
 */
@Configuration
@Data
@PropertySource("application.properties")
public class TelegramConfig {

    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String token;

    @Value("${bot.userId}")
    private Long userId;
}
