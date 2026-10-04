package com.pdd.pddbottg.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDto {
    String subscription;
    LocalDateTime subscriptionEndDate;
    boolean isActive;
    String message;
}
