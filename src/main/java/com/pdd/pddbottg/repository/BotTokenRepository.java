package com.pdd.pddbottg.repository;

import com.pdd.pddbottg.entity.BotTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BotTokenRepository extends JpaRepository<BotTokenEntity, Long> {
    Optional<BotTokenEntity> findByTelegramId(String telegramId);
}