package backend.academy.linktracker.bot.service.dialog;

import backend.academy.linktracker.bot.enumResponse.ResponseCode;
import backend.academy.linktracker.bot.service.LinkParser;
import backend.academy.linktracker.bot.service.MessageService;
import backend.academy.linktracker.bot.service.StateService;
import backend.academy.linktracker.bot.state.State;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinksStageHandler implements DialogStageHandler {
    private final LinkParser linkParser;
    private final StateService stateService;
    private final MessageService messageService;

    @Override
    public boolean supports(State state) {
        return state == State.WAITING_FOR_LINK;
    }

    @Override
    public SendMessage handle(long chatId, String text) {
        try {
            URI parsedUrl = linkParser.parse(text);
            stateService.updateContext(chatId, context -> {
                context.setPendingUrl(parsedUrl);
                context.setState(State.WAITING_FOR_TAGS);
            });
            log.info("link accepted for chatid {}: {}", chatId, parsedUrl);
            return new SendMessage(chatId, messageService.getMessage(ResponseCode.LINK_ACCEPTED));
        } catch (URISyntaxException e) {
            log.warn("invalid link format from chatid {}: {}", chatId, text);
            return new SendMessage(chatId, messageService.getMessage(ResponseCode.INVALID_LINK_FORMAT));
        } catch (Exception e) {
            log.error("unexpected error in link handler for chatid {}", chatId, e);
            throw e;
        }
    }
}
