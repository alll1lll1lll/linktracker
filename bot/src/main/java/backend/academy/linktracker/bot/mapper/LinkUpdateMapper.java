package backend.academy.linktracker.bot.mapper;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.grpc.LinkUpdateMsg;
import java.net.URI;
import org.springframework.stereotype.Component;

@Component
public class LinkUpdateMapper {
    public LinkUpdate toDto(LinkUpdateMsg msg) {
        return LinkUpdate.builder()
                .url(URI.create(msg.getUrl()))
                .tgChatIds(msg.getTgChatIdsList())
                .description(msg.getDescription())
                .build();
    }
}
