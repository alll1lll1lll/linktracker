package backend.academy.linktracker.bot.service.dialog;

import backend.academy.linktracker.bot.state.State;
import com.pengrad.telegrambot.request.SendMessage;

public interface DialogStageHandler {
    boolean supports(State state);

    SendMessage handle(long chatId, String text);
}
