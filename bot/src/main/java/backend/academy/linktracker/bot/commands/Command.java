package backend.academy.linktracker.bot.commands;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Command {
    private String commandName;
    private String description;

    public Command(String commandName, String description) {
        this.commandName = commandName;
        this.description = description;
    }

    public SendMessage process(Update update){
        long chatId = update.message().chat().id();
        String message = update.message().text();
        return handle(update, chatId, message);
    }
    protected abstract SendMessage handle(Update update, long chatId, String text);
}
