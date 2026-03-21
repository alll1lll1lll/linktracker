package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.commands.CommandType;
import backend.academy.linktracker.bot.enumResponse.ResponseCode;
import backend.academy.linktracker.bot.service.dialog.DialogService;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateRouter {
    private final CommandService commandService;
    private final DialogService dialogService;
    private final StateService stateService;
    private final MessageService messageService;
    private final CommandParser commandParser;

    public SendMessage route(Update update) {
        long chatId = update.message().chat().id();
        String text = update.message().text().trim();
        if (text.startsWith("/")) {
            String commandName = commandParser.extractCommandName(text);

            if (stateService.isInDialog(chatId) && !commandName.equals(CommandType.CANCEL.getCommandName())) {
                log.atInfo().log("User {} interrupted dialog with command {}", chatId, commandName);
                stateService.reset(chatId);
            }
            return commandService.process(commandName, text, update, chatId);
        }

        if (stateService.isInDialog(chatId)) {
            return dialogService.processDialog(chatId, text);
        }

        return new SendMessage(chatId, messageService.getMessage(ResponseCode.PLEASE_USE_HELP));
    }
}
