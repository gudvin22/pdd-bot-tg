package com.pdd.pddbottg.service;

import com.pdd.pddbottg.entity.BotTokenEntity;
import com.pdd.pddbottg.repository.BotTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenStorageService {

    private  final BotTokenRepository botTokenRepository;

    public void SaveToken(String telegramId, String jwtToken) {

        BotTokenEntity entity = botTokenRepository.findByTelegramId(telegramId)
                .orElse(new BotTokenEntity());
        entity.setTelegramId(telegramId);
        entity.setJwtToken(jwtToken);
        botTokenRepository.save(entity);

    }

    public Optional<String>GetJwtToken(String telegramId) {
        return botTokenRepository.findByTelegramId(telegramId)
                .map(BotTokenEntity::getJwtToken);
    }
}
