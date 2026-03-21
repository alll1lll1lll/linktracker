package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.dto.LinkResponse;
import backend.academy.linktracker.bot.dto.ListLinksResponse;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ResponseFormatter {
    public boolean isResponseEmpty(ListLinksResponse response) {
        return response == null
                || response.getLinks() == null
                || response.getLinks().isEmpty();
    }

    public List<LinkResponse> filterLinksByTag(List<LinkResponse> links, String tag) {
        if (tag == null) {
            return links;
        }
        return links.stream()
                .filter(link -> link.getTags() != null && link.getTags().contains(tag))
                .toList();
    }

    public String buildLinksListMessage(List<LinkResponse> links, Optional<String> tag) {
        StringBuilder sb = new StringBuilder();

        tag.ifPresentOrElse(
                t -> sb.append("Ваши ссылки с тегом ").append(t).append(":\n"), () -> sb.append("Ваши ссылки:\n"));

        for (LinkResponse link : links) {
            sb.append("- ").append(link.getUrl());
            if (link.getTags() != null && !link.getTags().isEmpty()) {
                sb.append(" [").append(String.join(", ", link.getTags())).append("]");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
