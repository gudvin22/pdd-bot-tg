package com.pdd.pddbottg.service;

import com.pdd.pddbottg.dto.TelegramRegistrationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AuthService {
    @Value("${myserver.address}")
    private String serverAddress;

    private final RestTemplate restTemplate;



    public String registerUser(String telegramId, String userName) {
        String url = serverAddress + "api/auth/telegram-register";

        TelegramRegistrationDto dto = new TelegramRegistrationDto();
        dto.setTelegramId(telegramId);
        dto.setUserName(userName);

        try {
            return restTemplate.postForObject(url, dto, String.class);
        } catch (RestClientException e) {
            throw new RuntimeException(e);

        }

    }

}