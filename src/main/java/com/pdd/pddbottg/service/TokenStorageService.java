package com.pdd.pddbottg.service;

import com.pdd.pddbottg.entity.BotTokenEntity;
import com.pdd.pddbottg.repository.BotTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpHeaders;
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

    public Optional<String>getJwtToken(String telegramId) {
        return botTokenRepository.findByTelegramId(telegramId)
                .map(BotTokenEntity::getJwtToken);
    }

    public HttpHeaders createAuthHeaders(String telegramId) {
        String token = getJwtToken(telegramId)
                .orElseThrow(() -> new RuntimeException("Токен не найден"));
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }
}
