package com.ua.osa.tradingbot.models.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Entity
@Table(name = "strategy_statistic")
@Data
public class StrategyStatistic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date timestamp;

    @Column(name = "close_price")
    private BigDecimal closePrice;

    @Column(name = "ai")
    private int ai;

    @Column(name = "ma")
    private int ma;

    @Column(name = "bb")
    private int bb;

    @Column(name = "rsi")
    private int rsi;

    @Column(name = "macd")
    private int macd;

    @Column(name = "stock_rsi")
    private int stockRsi;

    @Column(name = "adx")
    private int adx;
}
