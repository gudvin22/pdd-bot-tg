package com.pdd.pddbottg.handlers;

import com.pdd.pddbottg.PddBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateHandler {
    boolean handle(PddBot bot, Update update);
}