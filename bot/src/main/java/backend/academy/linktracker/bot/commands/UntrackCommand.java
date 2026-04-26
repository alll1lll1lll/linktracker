package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.service.dialog.StateService;
import backend.academy.linktracker.bot.state.State;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;

@Component
public class UntrackCommand extends Command {
    private final StateService stateService;

    public UntrackCommand(StateService stateService) {
        super(CommandType.UNTRACK);
        this.stateService = stateService;
    }

    @Override
    public SendMessage handle(Update update, long chatId, String text) {
        stateService.get(chatId).state = State.WAITING_FOR_UNTRACK;
        return new SendMessage(chatId, "введите ссылку, которую хотите перестать отслеживать");
    }
}
