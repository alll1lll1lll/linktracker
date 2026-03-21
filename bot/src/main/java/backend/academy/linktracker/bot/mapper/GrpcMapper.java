package backend.academy.linktracker.bot.mapper;

import backend.academy.linktracker.bot.dto.AddLinkRequest;
import backend.academy.linktracker.bot.dto.RemoveLinkRequest;
import backend.academy.linktracker.grpc.*;
import org.springframework.stereotype.Component;

@Component
public class GrpcMapper {
    public ChatIdRequest toChatIdRequest(long chatId) {
        return ChatIdRequest.newBuilder().setId(chatId).build();
    }

    public AddLinkRequestMsg toAddLinkMsg(AddLinkRequest request) {
        return AddLinkRequestMsg.newBuilder()
                .setLink(request.getLink().toString())
                .addAllTags(request.getTags() == null ? java.util.List.of() : request.getTags())
                .build();
    }

    public RemoveLinkRequestMsg toRemoveLinkMsg(RemoveLinkRequest request) {
        return RemoveLinkRequestMsg.newBuilder()
                .setLink(request.getLink().toString())
                .build();
    }
}
