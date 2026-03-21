package backend.academy.linktracker.bot.service;

import static org.slf4j.MDC.putCloseable;

import backend.academy.linktracker.bot.commands.Command;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ListenerService {

    private final TelegramBot telegramBot;
    private final UpdateRouter updateRouter;
    private final CommandService commandService;

    @EventListener(ApplicationReadyEvent.class)
    public void run() {
        setBotCommands();
        telegramBot.setUpdatesListener(
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
        updates.forEach(this::processUpdate);
    }

    private void processUpdate(Update update) {
        try (MDC.MDCCloseable _ = putCloseable("update_id", String.valueOf(update.updateId()))) {
            handleMessage(update);
        } catch (Exception e) {
            log.error("error update", e);
        }
    }

    private void handleMessage(Update update) {
        if (update.message() == null || update.message().text() == null) {
            return;
        }
        long chatId = update.message().chat().id();
        String text = update.message().text();

        try (MDC.MDCCloseable _ = putCloseable("chat_id", String.valueOf(chatId))) {
            log.info("new message received: '{}'", text);
            SendMessage responseMessage = updateRouter.route(update);
            sendResponse(responseMessage);
        }
    }

    private void sendResponse(SendMessage responseMessage) {
        if (responseMessage == null) return;

        BaseResponse response = telegramBot.execute(responseMessage);
        if (!response.isOk()) {
            log.atError()
                    .addKeyValue("error_code", response.errorCode())
                    .addKeyValue("description", response.description())
                    .log("error to send message to tg");
        }
    }

    private void setBotCommands() {
        List<Command> commandList = commandService.getAllCommandList();
        BotCommand[] botCommands = commandList.stream()
                .map(command -> new BotCommand(
                        command.getCommandType().getCommandName(),
                        command.getCommandType().getDescription()))
                .toArray(BotCommand[]::new);

        BaseResponse response = telegramBot.execute(new SetMyCommands(botCommands));
        if (response.isOk()) {
            log.atInfo().addKeyValue("bot_commands_status", "set").log("Bot commands menu updated");
        } else {
            log.atError().addKeyValue("error_code", response.errorCode()).log("Failed to set bot commands");
        }
    }
}
