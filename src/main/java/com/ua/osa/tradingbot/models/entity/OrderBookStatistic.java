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
@Table(name = "orderbook_statistic")
@Data
public class OrderBookStatistic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date timestamp;

    @Column(name = "close_price")
    private BigDecimal closePrice;

    @Column(name = "bids_total_amount")
    private BigDecimal bidsTotalAmount;

    @Column(name = "asks_total_amount")
    private BigDecimal asksTotalAmount;

    @Column(name = "is_open")
    private Boolean isOpen;
}
