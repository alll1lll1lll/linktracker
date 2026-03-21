package backend.academy.linktracker.scrapper.grpc;

import backend.academy.linktracker.grpc.LinkResponseMsg;
import backend.academy.linktracker.scrapper.dto.response.LinkResponse;
import java.util.Collections;
import org.springframework.stereotype.Component;

@Component
public class GrpcMapper {
    public LinkResponseMsg toMsg(LinkResponse response) {
        return LinkResponseMsg.newBuilder()
                .setId(response.getId())
                .setUrl(response.getUrl().toString())
                .addAllTags(response.getTags() == null ? Collections.emptyList() : response.getTags())
                .build();
    }
}
