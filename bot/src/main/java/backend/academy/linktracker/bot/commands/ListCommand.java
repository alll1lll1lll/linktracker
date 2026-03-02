package backend.academy.linktracker.bot.commands;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

public class ListCommand extends Command {
    public ListCommand(String commandName, String description) {
        super("/list", "/ вывести список всех ссылок");
    }

    @Override
    public SendMessage handle(Update update, long chatId, String text) {
        return null; // todo
    }
}
