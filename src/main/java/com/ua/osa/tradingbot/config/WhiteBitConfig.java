package com.ua.osa.tradingbot.config;

import com.ua.osa.tradingbot.restClients.AuthInterceptor;
import com.ua.osa.tradingbot.restClients.WhiteBitClient;
import feign.Feign;
import feign.Logger;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.slf4j.Slf4jLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WhiteBitConfig {

    @Bean
    public WhiteBitClient whiteBitClient() {
        return Feign.builder()
                .decoder(new GsonDecoder())
                .encoder(new GsonEncoder())
                .requestInterceptor(new AuthInterceptor())
                .logger(new Slf4jLogger())
                .logLevel(Logger.Level.FULL)
                .target(WhiteBitClient.class, "https://whitebit.com");
    }
}
