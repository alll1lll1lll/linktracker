package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.service.dialog.StateService;
import backend.academy.linktracker.bot.state.State; // <-- Добавили импорт
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;

@Component
public class TrackCommand extends Command {
    private final StateService stateService;

    public TrackCommand(StateService stateService) {
        super(CommandType.TRACK);
        this.stateService = stateService;
    }

    @Override
    public SendMessage handle(Update update, long chatId, String text) {
        stateService.get(chatId).state = State.WAITING_FOR_LINK;
        return new SendMessage(chatId, "введите ссылку для отслеживания:");
    }
}
