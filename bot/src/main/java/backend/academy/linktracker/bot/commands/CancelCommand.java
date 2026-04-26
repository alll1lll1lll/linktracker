package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.service.dialog.StateService;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;

@Component
public class CancelCommand extends Command {
    private final StateService stateService;

    public CancelCommand(StateService service) {
        super(CommandType.CANCEL);
        this.stateService = service;
    }

    @Override
    public SendMessage handle(Update update, long chatId, String text) {
        stateService.reset(chatId);
        return new SendMessage(chatId, "действие отменено");
    }
}
