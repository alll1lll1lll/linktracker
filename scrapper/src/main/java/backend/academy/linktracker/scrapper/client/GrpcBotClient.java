package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.grpc.BotServiceGrpc;
import backend.academy.linktracker.grpc.LinkUpdateMsg;
import backend.academy.linktracker.scrapper.client.interfaces.BotClient;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import io.grpc.ManagedChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.client-type", havingValue = "grpc")
public class GrpcBotClient implements BotClient {

    private final BotServiceGrpc.BotServiceBlockingStub botStub;

    public GrpcBotClient(GrpcChannelFactory channelFactory) {
        ManagedChannel channel = channelFactory.createChannel("bot-service");
        this.botStub = BotServiceGrpc.newBlockingStub(channel);
        log.info("BotClient is active. Using: gRPC protocol");
    }

    @Override
    public void sendUpdate(LinkUpdate update) {
        LinkUpdateMsg request = LinkUpdateMsg.newBuilder()
                .setId(update.getId())
                .setUrl(update.getUrl().toString())
                .setDescription(update.getDescription())
                .addAllTgChatIds(update.getTgChatIds())
                .build();

        botStub.sendUpdate(request);
    }
}
