package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.dto.LinkResponse;
import backend.academy.linktracker.bot.dto.ListLinksResponse;
import backend.academy.linktracker.bot.exception.ServiceUnavailableException;
import backend.academy.linktracker.bot.service.ResponseFormatter;
import backend.academy.linktracker.bot.service.TagService;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ListCommand extends Command {
    private final ScrapperClient scrapperClient;
    private final TagService tagService;
    private final ResponseFormatter responseFormatter;

    public ListCommand(ScrapperClient scrapperClient, TagService tagService, ResponseFormatter responseFormatter) {
        super(CommandType.LIST);
        this.scrapperClient = scrapperClient;
        this.tagService = tagService;
        this.responseFormatter = responseFormatter;
    }

    @Override
    public SendMessage handle(Update update, long chatId, String text) {
        try {
            Optional<String> filterTag = tagService.extractTagFromCommand(text);
            ListLinksResponse response = scrapperClient.getLinks(chatId);
            if (responseFormatter.isResponseEmpty(response)) {
                return new SendMessage(chatId, "у вас нет активных подписок.");
            }
            List<LinkResponse> filteredLinks =
                    responseFormatter.filterLinksByTag(response.getLinks(), filterTag.orElse(null));

            if (filteredLinks.isEmpty()) {
                return filterTag
                        .map(tag -> new SendMessage(chatId, "у вас нет активных подписок с тегом: " + tag))
                        .orElseGet(() -> new SendMessage(chatId, "у вас нет активных подписок."));
            }
            String messageText = responseFormatter.buildLinksListMessage(filteredLinks, filterTag);
            return new SendMessage(chatId, messageText);

        } catch (ServiceUnavailableException e) {
            log.error("Scrapper service is unavailable for chat {}", chatId, e);
            return new SendMessage(chatId, "извините, сервис временно недоступен. попробуйте позже.");

        } catch (Exception e) {
            log.error("Failed to execute /list for chat {}", chatId, e);
            return new SendMessage(chatId, "произошла ошибка получения списка. пожалуйста, попробуйте позже.");
        }
    }

    @Override
    public boolean acceptsArguments() {
        return true;
    }
}
