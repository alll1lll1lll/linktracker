package backend.academy.linktracker.bot.service.dialog;

import backend.academy.linktracker.bot.enumResponse.ResponseCode;
import backend.academy.linktracker.bot.service.MessageService;
import backend.academy.linktracker.bot.service.StateService;
import backend.academy.linktracker.bot.service.TagService;
import backend.academy.linktracker.bot.service.TrackService;
import backend.academy.linktracker.bot.state.State;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TagsStageHandler implements DialogStageHandler {
    private final TagService tagService;
    private final TrackService trackService;
    private final StateService stateService;
    private final MessageService messageService;

    @Override
    public boolean supports(State state) {
        return state == State.WAITING_FOR_TAGS;
    }

    @Override
    public SendMessage handle(long chatId, String text) {
        try {
            List<String> tags = tagService.parseMultipleTags(text);
            URI pendingUrl = stateService.get(chatId).getPendingUrl();

            ResponseCode code = trackService.track(chatId, pendingUrl, tags);
            stateService.reset(chatId);

            log.info("tracking completed for chatid {} with url {}", chatId, pendingUrl);
            return new SendMessage(chatId, messageService.getMessage(code));
        } catch (Exception e) {
            log.error("failed to track link for chatid {}", chatId, e);
            return new SendMessage(chatId, "произошла ошибка при сохранении ссылки.");
        }
    }
}
