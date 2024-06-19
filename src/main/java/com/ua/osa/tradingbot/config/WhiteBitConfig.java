package com.ua.osa.tradingbot.config;

import com.ua.osa.tradingbot.restClients.WhiteBitClient;
import feign.Feign;
import feign.gson.GsonDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WhiteBitConfig {

    @Bean
    public WhiteBitClient whiteBitClient() {
        return Feign.builder()
                .decoder(new GsonDecoder())
                .target(WhiteBitClient.class, "https://whitebit.com");
    }
}
