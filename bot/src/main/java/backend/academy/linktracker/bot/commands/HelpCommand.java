package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.service.CommandService;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Lazy;

@Component
public class HelpCommand extends Command{
    private final CommandService commandService;
    public HelpCommand(@Lazy CommandService commandService) {
       super("/help", "Список доступных для использования команд");
       this.commandService = commandService;
    }

    @Override
    protected SendMessage handle(Update update, long chatId, String text) {
        String responseText = "Доступные команды:\n" +
            commandService.getAllCommandList().stream()
                .map(command -> command.getCommandName() + " - " + command.getDescription())
                .collect(Collectors.joining("\n"));
        return new SendMessage(chatId, responseText);
    }
}
