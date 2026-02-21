package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.commands.Command;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ListenerService {
    private final TelegramBot telegramBot;
    private final CommandService commandService;

    @Autowired
    public ListenerService(TelegramBot telegramBot, CommandService commandService){
        this.commandService = commandService;
        this.telegramBot =telegramBot;
    }

    @PostConstruct
    public void run() {
        this.setBotCommands();
        this.telegramBot.setUpdatesListener(
            updates -> {
                processUpdates(updates);
                return UpdatesListener.CONFIRMED_UPDATES_ALL;
            },
            e -> {
                if (e.response() != null) {
                    System.err.println("Telegram API error: " + e.response().errorCode() + " " + e.response().description());
                } else {
                    System.err.println("Network error or unexpected exception: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        );
    }

    private void processUpdates(List<Update> updates) {
        for (Update update : updates) {
            if (update.message() != null && update.message().text() != null) {
                long chatId = update.message().chat().id();
                String text = update.message().text();
                SendMessage responseMessage;
                if (text.startsWith("/")) {
                    responseMessage = commandService.processCommand(text, update, chatId);
                } else {
                    responseMessage = new SendMessage(chatId, "Я не понимаю эту команду. Используйте /help для просмотра списка доступных команд.");
                }
                if (responseMessage != null) {
                    BaseResponse response = telegramBot.execute(responseMessage);
                }
            }
        }
    }
    private void setBotCommands() {
        List<Command> commandList = commandService.getAllCommandList();
        BotCommand[] botCommands = commandList.stream()
            .map(command -> new BotCommand(command.getCommandName(), command.getDescription()))
            .toArray(BotCommand[]::new);
        SetMyCommands myCommands = new SetMyCommands(botCommands);
        telegramBot.execute(myCommands);
        System.out.println("Bot commands menu has been set.");
    }
}
