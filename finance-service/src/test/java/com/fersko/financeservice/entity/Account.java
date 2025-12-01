package com.fersko.financeservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@lombok.Getter
@lombok.Setter
@Entity
@Table(name = "accounts", schema = "finance")
public class Account {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "balance", precision = 18, scale = 2)
    private BigDecimal balance;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

}