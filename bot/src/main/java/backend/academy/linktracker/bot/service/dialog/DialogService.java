package backend.academy.linktracker.bot.service.dialog;

import backend.academy.linktracker.bot.enumResponse.ResponseCode;
import backend.academy.linktracker.bot.service.MessageService;
import backend.academy.linktracker.bot.service.StateService;
import backend.academy.linktracker.bot.state.State;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DialogService {
    private final StateService stateService;
    private final MessageService messageService;
    private final List<DialogStageHandler> handlers;

    public SendMessage processDialog(long chatId, String text) {
        State currentState = stateService.get(chatId).getState();
        log.debug("processing dialog for chatid {}, state: {}", chatId, currentState);

        return handlers.stream()
                .filter(h -> h.supports(currentState))
                .findFirst()
                .map(h -> h.handle(chatId, text))
                .orElseGet(() -> {
                    log.error("no handler found for state {} in chatid {}", currentState, chatId);
                    return new SendMessage(chatId, messageService.getMessage(ResponseCode.PLEASE_USE_HELP));
                });
    }
}
