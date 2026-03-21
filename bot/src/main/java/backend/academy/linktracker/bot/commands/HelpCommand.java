package backend.academy.linktracker.bot.commands;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class HelpCommand extends Command {

    public HelpCommand() {
        super(CommandType.HELP);
    }

    @Override
    public SendMessage handle(Update update, long chatId, String text) {
        String responseText = "Доступные команды:\n"
                + Arrays.stream(CommandType.values())
                        .map(cmd -> cmd.getCommandName() + " — " + cmd.getDescription())
                        .collect(Collectors.joining("\n"));

        return new SendMessage(chatId, responseText);
    }
}
