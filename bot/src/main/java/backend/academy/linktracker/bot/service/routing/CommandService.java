package backend.academy.linktracker.bot.service.routing;

import backend.academy.linktracker.bot.commands.Command;
import backend.academy.linktracker.bot.enumResponse.ResponseCode;
import backend.academy.linktracker.bot.service.format.MessageService;
import backend.academy.linktracker.bot.service.parser.CommandParser;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CommandService {
    private final Map<String, Command> commands;
    private final MessageService messageService;
    private final CommandParser commandParser;

    @Getter
    private final List<Command> allCommandList;

    public CommandService(List<Command> commandList, MessageService messageService, CommandParser commandParser) {
        this.commands = commandList.stream()
                .collect(Collectors.toMap(cmd -> cmd.getCommandType().getCommandName(), Function.identity()));
        this.allCommandList = commandList;
        this.messageService = messageService;
        this.commandParser = commandParser;
    }

    public SendMessage process(String commandName, String fullText, Update update, long chatId) {
        Command command = commands.get(commandName);
        if (command == null) {
            log.atWarn().addKeyValue("command", commandName).log("Unknown command");
            return new SendMessage(chatId, messageService.getMessage(ResponseCode.UNKNOWN_COMMAND));
        }
        boolean hasArguments = commandParser.hasArguments(fullText);

        if (hasArguments && !command.acceptsArguments()) {
            log.atWarn()
                    .addKeyValue("command", commandName)
                    .addKeyValue("fullText", fullText)
                    .log("Command doesn't accept arguments");

            return new SendMessage(chatId, messageService.getMessage(ResponseCode.UNKNOWN_COMMAND));
        }

        log.atInfo().log("Executing command: {}", commandName);
        return command.handle(update, chatId, fullText);
    }
}
