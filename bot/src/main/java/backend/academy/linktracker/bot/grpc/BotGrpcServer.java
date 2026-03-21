package backend.academy.linktracker.bot.grpc;

import backend.academy.linktracker.bot.mapper.LinkUpdateMapper;
import backend.academy.linktracker.bot.service.NotificationService;
import backend.academy.linktracker.grpc.BotServiceGrpc;
import backend.academy.linktracker.grpc.Empty;
import backend.academy.linktracker.grpc.LinkUpdateMsg;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class BotGrpcServer extends BotServiceGrpc.BotServiceImplBase {

    private final NotificationService notificationService;
    private final LinkUpdateMapper mapper;

    @Override
    public void sendUpdate(LinkUpdateMsg request, StreamObserver<Empty> responseObserver) {
        notificationService.processUpdate(mapper.toDto(request));
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }
}
