package com.pdd.pddbottg.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "bot_token")
public class BotTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telegram_id", nullable = false, unique = true)
    private String telegramId;

    @Column(name = "jwt_token", nullable = false)
    private String jwtToken;

}