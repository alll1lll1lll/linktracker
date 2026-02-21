package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.commands.Command;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.Getter;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CommandService {
    private final Map<String, Command> commands;
    @Getter
    private final List<Command> allCommandList;

    public CommandService(List<Command> commandList) {
        this.commands = commandList.stream()
            .collect(Collectors.toMap(Command::getCommandName, Function.identity()));
        this.allCommandList = commandList;
    }

    public SendMessage processCommand(String text, Update update, long chatId){
        Command command = commands.get(text);
        if (command != null){
            return command.process(update);
        } else{
            return new SendMessage(chatId, "Неизвестная команда, используйте /help");
        }
    }
}
