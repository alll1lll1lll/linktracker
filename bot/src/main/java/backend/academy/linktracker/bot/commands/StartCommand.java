package backend.academy.linktracker.bot.commands;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;

@Component
public class StartCommand extends Command{
    public StartCommand() {
        super("/start", "Начало работы бота");
    }

    @Override
    protected SendMessage handle(Update update, long chatId, String text) {
        String responseText = "Добро пожаловать! Я — бот для отслеживания ссылок.\n" +
            "Используйте /help, чтобы посмотреть доступные команды.";
        return new SendMessage(chatId, responseText);
    }
}
