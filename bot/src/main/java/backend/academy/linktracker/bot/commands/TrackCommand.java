package backend.academy.linktracker.bot.commands;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

public class TrackCommand extends Command {
    public TrackCommand(String commandName, String description) {
        super("/track", "Начать отслеживавние ссылки");
    }

    @Override
    public SendMessage handle(Update update, long chatId, String text) {
        return null; // todo
    }
}
