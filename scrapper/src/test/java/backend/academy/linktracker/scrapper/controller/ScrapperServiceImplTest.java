package backend.academy.linktracker.scrapper.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;

import backend.academy.linktracker.grpc.*;
import backend.academy.linktracker.scrapper.exception.ChatAlreadyRegisteredException;
import backend.academy.linktracker.scrapper.grpc.GrpcMapper;
import backend.academy.linktracker.scrapper.grpc.ScrapperServiceImpl;
import backend.academy.linktracker.scrapper.service.LinksService;
import backend.academy.linktracker.scrapper.service.TgChatService;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ScrapperServiceImplTest {

    private Server inProcessServer;
    private ManagedChannel inProcessChannel;
    private ScrapperServiceGrpc.ScrapperServiceBlockingStub blockingStub;

    private TgChatService tgChatService;
    private LinksService linksService;
    private GrpcMapper mapper;

    @BeforeEach
    void setup() throws IOException {
        tgChatService = Mockito.mock(TgChatService.class);
        linksService = Mockito.mock(LinksService.class);
        mapper = new GrpcMapper();

        ScrapperServiceImpl scrapperService = new ScrapperServiceImpl(tgChatService, linksService, mapper);

        String serverName = InProcessServerBuilder.generateName();

        inProcessServer = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(scrapperService)
                .build()
                .start();

        inProcessChannel =
                InProcessChannelBuilder.forName(serverName).directExecutor().build();

        blockingStub = ScrapperServiceGrpc.newBlockingStub(inProcessChannel);
    }

    @AfterEach
    void teardown() {
        inProcessChannel.shutdownNow();
        inProcessServer.shutdownNow();
    }

    @Test
    void registerChat_ShouldSucceed() {
        ChatIdRequest request = ChatIdRequest.newBuilder().setId(123L).build();

        assertDoesNotThrow(() -> blockingStub.registerChat(request));

        Mockito.verify(tgChatService).register(123L);
    }

    @Test
    void registerChat_ShouldReturnAlreadyExists_WhenExceptionThrown() {
        long chatId = 123L;
        doThrow(new ChatAlreadyRegisteredException(chatId)).when(tgChatService).register(anyLong());

        ChatIdRequest request = ChatIdRequest.newBuilder().setId(123L).build();

        StatusRuntimeException exception =
                assertThrows(StatusRuntimeException.class, () -> blockingStub.registerChat(request));

        assertEquals(io.grpc.Status.Code.ALREADY_EXISTS, exception.getStatus().getCode());
        assertTrue(exception.getMessage().contains("чат с id " + chatId + " уже зарегистрирован"));
    }
}
