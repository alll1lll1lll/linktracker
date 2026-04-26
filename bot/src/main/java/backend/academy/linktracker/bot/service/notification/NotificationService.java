package backend.academy.linktracker.bot.service.notification;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
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
            log.atInfo().log("attempting to send TG message to chatId: {}", chatId);

            String message =
                    String.format("появилось обновление по ссылке: %s: %s", update.getUrl(), update.getDescription());

            SendResponse response = telegramBot.execute(new SendMessage(chatId, message));

            if (response.isOk()) {
                log.atInfo().log("successfully sent message to Telegram for chat {}", chatId);
            } else {
                log.atError().log("telegram API Error: {} - {}", response.errorCode(), response.description());
            }
        }
    }
}
