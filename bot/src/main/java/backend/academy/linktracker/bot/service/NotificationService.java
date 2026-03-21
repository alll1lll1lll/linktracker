package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final TelegramBot telegramBot;

    public void processUpdate(LinkUpdate update) {

        for (long chatId : update.getTgChatIds()) {
            String message =
                    String.format("появилось обновление по ссылке: %s: %s", update.getUrl(), update.getDescription());

            telegramBot.execute(new SendMessage(chatId, message));
        }
    }
}
