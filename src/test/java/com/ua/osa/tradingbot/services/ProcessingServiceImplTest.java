package com.ua.osa.tradingbot.services;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProcessingServiceImplTest {

    @Test
    void name() {
        long up = 15;
        long down = 8;
        if (up > 5 && down > 5) {
            long upPercent = up * 100 / (up + down);
            System.out.println(upPercent);
        }
    }

    @Test
    void name2() {
        assertEquals(BigDecimal.valueOf(10), BigDecimal.valueOf(10).divide(BigDecimal.valueOf(7), 6, BigDecimal.ROUND_DOWN));
    }
}