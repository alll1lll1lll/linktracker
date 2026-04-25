package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.client.interfaces.BotClient;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.model.LinkModel;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkErrorReportService {

    private final SubscriptionRepository subscriptionRepository;
    private final BotClient botClient;

    public void sendErrorReport(List<LinkModel> failedLinks) {
        if (failedLinks == null || failedLinks.isEmpty()) {
            return;
        }

        for (LinkModel link : failedLinks) {
            processSingleLinkError(link);
        }

        log.atInfo().addKeyValue("failed_links_count", failedLinks.size()).log("error reports processing finished");
    }

    private void processSingleLinkError(LinkModel link) {
        List<Long> chatIds = subscriptionRepository.findChatSubscribers(link.getId());

        if (chatIds.isEmpty()) {
            log.atDebug().addKeyValue("link_id", link.getId()).log("no subscribers for failed link");
            return;
        }

        String errorMessage = formatErrorMessage(link);

        LinkUpdate update = new LinkUpdate(link.getId(), link.getUrl(), errorMessage, chatIds);

        try {
            botClient.sendUpdate(update);
        } catch (Exception e) {
            log.atError()
                    .addKeyValue("link_id", link.getId())
                    .setCause(e)
                    .log("failed to deliver error report for link");
        }
    }

    private String formatErrorMessage(LinkModel link) {
        return String.format(
                "ошибка обновления ссылки**%n%n"
                        + "не удалось получить актуальные данные для ресурса:%n%s%n%n"
                        + "мы автоматически повторим попытку в следующем цикле обновления.",
                link.getUrl());
    }
}
