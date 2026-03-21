package backend.academy.linktracker.bot.service.dialog;

import backend.academy.linktracker.bot.enumResponse.ResponseCode;
import backend.academy.linktracker.bot.service.MessageService;
import backend.academy.linktracker.bot.service.StateService;
import backend.academy.linktracker.bot.service.TrackService;
import backend.academy.linktracker.bot.state.State;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UntrackStageHandler implements DialogStageHandler {
    private final TrackService trackService;
    private final StateService stateService;
    private final MessageService messageService;

    @Override
    public boolean supports(State state) {
        return state == State.WAITING_FOR_UNTRACK;
    }

    @Override
    public SendMessage handle(long chatId, String text) {
        try {
            ResponseCode code = trackService.untrack(chatId, text);
            stateService.reset(chatId);

            log.info("untrack processed for chatid {}, result: {}", chatId, code);
            return new SendMessage(chatId, messageService.getMessage(code));
        } catch (Exception e) {
            log.error("error during untrack for chatid {}:", chatId, e);
            throw e;
        }
    }
}
