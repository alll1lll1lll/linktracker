package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.commands.Command;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class ListenerService {
    private final TelegramBot telegramBot;
    private final CommandService commandService;

    @EventListener(ApplicationReadyEvent.class)
    public void run() {
        this.setBotCommands();
        this.telegramBot.setUpdatesListener(
                updates -> {
                    processUpdates(updates);
                    return UpdatesListener.CONFIRMED_UPDATES_ALL;
                },
                e -> log.atError()
                        .addKeyValue("event", "telegram_api_error_listener")
                        .setCause(e)
                        .log("Error in Telegram updates listener"));
        log.atInfo().addKeyValue("bot_status", "started").log("Telegram bot listener initialized");
    }

    private void processUpdates(List<Update> updates) {
        for (Update update : updates) {
            try {
                if (update.message() != null && update.message().text() != null) {
                    long chatId = update.message().chat().id();
                    String text = update.message().text();

                    log.atInfo()
                            .addKeyValue("event", "received_message")
                            .addKeyValue("chat_id", chatId)
                            .addKeyValue("text", text)
                            .log("Received new message");

                    SendMessage responseMessage;
                    responseMessage = commandService.processCommand(text, update, chatId);
                    BaseResponse response = telegramBot.execute(responseMessage);
                    if (!response.isOk()) {
                        log.atError()
                                .addKeyValue("event", "telegram_send_error")
                                .addKeyValue("error_code", response.errorCode())
                                .addKeyValue("description", response.description())
                                .addKeyValue("chat_id", chatId)
                                .log("Failed to send message to Telegram");
                    }
                }
            } catch (Exception e) {
                log.atError()
                        .addKeyValue("event", "update_processing_error")
                        .addKeyValue("update_id", update.updateId())
                        .setCause(e)
                        .log("Error processing update");
            }
        }
    }

    private void setBotCommands() {
        List<Command> commandList = commandService.getAllCommandList();
        BotCommand[] botCommands = commandList.stream()
                .map(command -> new BotCommand(command.getCommandName(), command.getDescription()))
                .toArray(BotCommand[]::new);
        SetMyCommands myCommands = new SetMyCommands(botCommands);
        BaseResponse response = telegramBot.execute(myCommands);
        if (response.isOk()) {
            log.atInfo()
                    .addKeyValue("bot_commands_status", "set")
                    .log("Bot commands menu has been successfully updated");
        } else {
            log.atError()
                    .addKeyValue("bot_commands_status", "failed")
                    .addKeyValue("error_code", response.errorCode())
                    .addKeyValue("description", response.description())
                    .log("Failed to set bot commands");
        }
    }
}
