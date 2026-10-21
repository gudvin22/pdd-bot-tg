package com.pdd.pddbottg;

import com.pdd.pddbottg.entity.BotTokenEntity;
import com.pdd.pddbottg.repository.BotTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenStorageServiceTest {

    @Mock
    private BotTokenRepository botTokenRepository;

    @InjectMocks
    private TokenStorageService tokenStorageService;

    // ========== Тесты для SaveToken ==========

    @Test
    void saveToken_shouldSaveNewToken_whenTokenDoesNotExist() {
        // 1. Подготовка
        String telegramId = "12345";
        String token = "jwt-token-abc";

        // Мокаем, что в базе еще нет токена для этого юзера
        when(botTokenRepository.findByTelegramId(telegramId)).thenReturn(Optional.empty());

        // 2. Действие (обрати внимание на большую букву S)
        tokenStorageService.SaveToken(telegramId, token);

        // 3. Проверка
        // Проверяем, что вызвался save и был передан объект с нужными полями
        verify(botTokenRepository, times(1)).save(argThat(entity ->
                entity.getTelegramId().equals(telegramId) &&
                        entity.getJwtToken().equals(token)
        ));
    }

    @Test
    void saveToken_shouldUpdateExistingToken_whenTokenExists() {
        // 1. Подготовка
        String telegramId = "12345";
        String newToken = "new-jwt-token";

        BotTokenEntity existingEntity = new BotTokenEntity();
        existingEntity.setTelegramId(telegramId);
        existingEntity.setJwtToken("old-token");

        // Мокаем, что токен уже есть в базе
        when(botTokenRepository.findByTelegramId(telegramId)).thenReturn(Optional.of(existingEntity));

        // 2. Действие
        tokenStorageService.SaveToken(telegramId, newToken);

        // 3. Проверка
        // Убеждаемся, что поле в старом объекте обновилось и он ушел в save
        assertEquals(newToken, existingEntity.getJwtToken());
        verify(botTokenRepository, times(1)).save(existingEntity);
    }

    // ========== Тесты для createAuthHeaders ==========

    @Test
    void createAuthHeaders_whenTokenExists_shouldReturnHeadersWithBearerToken() {
        // 1. Подготовка
        String telegramId = "12345";
        String token = "my-jwt-token";

        BotTokenEntity entity = new BotTokenEntity();
        entity.setTelegramId(telegramId);
        entity.setJwtToken(token);

        when(botTokenRepository.findByTelegramId(telegramId)).thenReturn(Optional.of(entity));

        // 2. Действие
        HttpHeaders headers = tokenStorageService.createAuthHeaders(telegramId);

        // 3. Проверка
        assertNotNull(headers);
        assertEquals("Bearer " + token, headers.getFirst("Authorization"));
    }

    @Test
    void createAuthHeaders_whenTokenNotFound_shouldThrowException() {
        // 1. Подготовка
        String telegramId = "unknown";
        when(botTokenRepository.findByTelegramId(telegramId)).thenReturn(Optional.empty());

        // 2. Действие + Проверка
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tokenStorageService.createAuthHeaders(telegramId);
        });

        assertEquals("Токен не найден", exception.getMessage());
    }
}