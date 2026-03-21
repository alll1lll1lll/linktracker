package backend.academy.linktracker.bot.client;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.linktracker.bot.dto.AddLinkRequest;
import backend.academy.linktracker.bot.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.bot.exception.LinkNotFoundException;
import backend.academy.linktracker.bot.mapper.GrpcMapper;
import backend.academy.linktracker.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GrpcScrapperClientTest {

    private GrpcScrapperClient client;
    private Server inProcessServer;
    private ManagedChannel inProcessChannel;

    private final ScrapperServiceGrpc.ScrapperServiceImplBase mockScrapperService =
            new ScrapperServiceGrpc.ScrapperServiceImplBase() {

                @Override
                public void registerChat(ChatIdRequest request, StreamObserver<ScrapperEmpty> responseObserver) {
                    if (request.getId() == 999L) {
                        responseObserver.onError(Status.ALREADY_EXISTS.asRuntimeException());
                    } else {
                        responseObserver.onNext(ScrapperEmpty.getDefaultInstance());
                        responseObserver.onCompleted();
                    }
                }

                @Override
                public void removeLink(RemoveLinkRequestMsg request, StreamObserver<LinkResponseMsg> responseObserver) {
                    responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
                }

                @Override
                public void addLink(AddLinkRequestMsg request, StreamObserver<LinkResponseMsg> responseObserver) {
                    responseObserver.onNext(LinkResponseMsg.newBuilder()
                            .setId(1L)
                            .setUrl(request.getLink())
                            .build());
                    responseObserver.onCompleted();
                }
            };

    @BeforeEach
    void setup() throws IOException {
        String serverName = InProcessServerBuilder.generateName();
        inProcessServer = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(mockScrapperService)
                .build()
                .start();

        inProcessChannel =
                InProcessChannelBuilder.forName(serverName).directExecutor().build();

        ScrapperServiceGrpc.ScrapperServiceBlockingStub stub = ScrapperServiceGrpc.newBlockingStub(inProcessChannel);
        client = new GrpcScrapperClient(stub, new GrpcMapper());
    }

    @AfterEach
    void teardown() {
        inProcessChannel.shutdownNow();
        inProcessServer.shutdownNow();
    }

    @Test
    void registerChat_ShouldSucceed() {
        assertDoesNotThrow(() -> client.register(123L));
    }

    @Test
    void registerChat_ShouldThrowException_WhenAlreadyExists() {
        assertThrows(ChatAlreadyExistsException.class, () -> client.register(999L));
    }

    @Test
    void removeLink_ShouldThrowException_WhenNotFound() {
        backend.academy.linktracker.bot.dto.RemoveLinkRequest request =
                new backend.academy.linktracker.bot.dto.RemoveLinkRequest(URI.create("https://github.com"));
        assertThrows(LinkNotFoundException.class, () -> client.removeLink(123L, request));
    }

    @Test
    void addLink_ShouldSucceed() {
        AddLinkRequest request = new AddLinkRequest(URI.create("https://test.com"), List.of());
        assertDoesNotThrow(() -> client.addLink(123L, request));
    }
}
