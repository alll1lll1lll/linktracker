package backend.academy.linktracker.bot.commands;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class HelpCommand extends Command {
    private final List<Command> commands;

    public HelpCommand(List<Command> commands) {
        super("/help", "Список доступных команд");
        this.commands = commands;
    }

    @Override
    public SendMessage handle(Update update, long chatId, String text) {
        String responseText = "Доступные команды:\n"
                + commands.stream()
                        .sorted(Comparator.comparing(Command::getCommandName)) // добавила сортировку команд
                        .map(command -> command.getCommandName() + " - " + command.getDescription())
                        .collect(Collectors.joining("\n"));
        return new SendMessage(chatId, responseText);
    }
}
