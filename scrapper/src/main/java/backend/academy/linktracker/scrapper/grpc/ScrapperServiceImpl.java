package backend.academy.linktracker.scrapper.grpc;

import static backend.academy.linktracker.scrapper.grpc.ChatIdServerInterceptor.CHAT_ID_KEY;

import backend.academy.linktracker.grpc.AddLinkRequestMsg;
import backend.academy.linktracker.grpc.ChatIdRequest;
import backend.academy.linktracker.grpc.LinkResponseMsg;
import backend.academy.linktracker.grpc.ListLinksResponseMsg;
import backend.academy.linktracker.grpc.RemoveLinkRequestMsg;
import backend.academy.linktracker.grpc.ScrapperEmpty;
import backend.academy.linktracker.grpc.ScrapperServiceGrpc;
import backend.academy.linktracker.scrapper.dto.request.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.request.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.dto.response.ListLinksResponse;
import backend.academy.linktracker.scrapper.exception.ChatAlreadyRegisteredException;
import backend.academy.linktracker.scrapper.exception.ChatNotFoundException;
import backend.academy.linktracker.scrapper.exception.InvalidRequestException;
import backend.academy.linktracker.scrapper.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.exception.LinkNotFoundException;
import backend.academy.linktracker.scrapper.service.LinksService;
import backend.academy.linktracker.scrapper.service.TgChatService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.net.URI;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService(interceptors = ChatIdServerInterceptor.class)
@RequiredArgsConstructor
public class ScrapperServiceImpl extends ScrapperServiceGrpc.ScrapperServiceImplBase {

    private final TgChatService chatService;
    private final LinksService linksService;
    private final GrpcMapper mapper;

    @Override
    public void registerChat(ChatIdRequest request, StreamObserver<ScrapperEmpty> responseObserver) {
        handle(responseObserver, () -> {
            chatService.register(request.getId());
            return ScrapperEmpty.getDefaultInstance();
        });
    }

    @Override
    public void deleteChat(ChatIdRequest request, StreamObserver<ScrapperEmpty> responseObserver) {
        handle(responseObserver, () -> {
            chatService.deleteChat(request.getId());
            return ScrapperEmpty.getDefaultInstance();
        });
    }

    @Override
    public void addLink(AddLinkRequestMsg request, StreamObserver<LinkResponseMsg> responseObserver) {
        handle(responseObserver, () -> {
            Long chatId = CHAT_ID_KEY.get();
            if (chatId == null) {
                throw Status.INTERNAL
                        .withDescription("chat is not found in context")
                        .asRuntimeException();
            }
            AddLinkRequest dto = new AddLinkRequest(URI.create(request.getLink()), request.getTagsList());
            return mapper.toMsg(linksService.add(chatId, dto));
        });
    }

    @Override
    public void removeLink(RemoveLinkRequestMsg request, StreamObserver<LinkResponseMsg> responseObserver) {
        handle(responseObserver, () -> {
            Long chatId = CHAT_ID_KEY.get();
            if (chatId == null) {
                throw Status.INTERNAL
                        .withDescription("chat id not found in context")
                        .asRuntimeException();
            }
            RemoveLinkRequest dto = new RemoveLinkRequest(URI.create(request.getLink()));
            return mapper.toMsg(linksService.remove(chatId, dto));
        });
    }

    @Override
    public void getLinks(ChatIdRequest request, StreamObserver<ListLinksResponseMsg> responseObserver) {
        handle(responseObserver, () -> {
            Long chatId = CHAT_ID_KEY.get();
            if (chatId == null) {
                throw Status.INTERNAL
                        .withDescription("chat is not found in context")
                        .asRuntimeException();
            }

            ListLinksResponse response = linksService.listAll(chatId);

            var builder =
                    ListLinksResponseMsg.newBuilder().setSize(response.getSize() != null ? response.getSize() : 0);
            if (response.getLinks() != null) {
                response.getLinks().forEach(link -> builder.addLinks(mapper.toMsg(link)));
            }
            return builder.build();
        });
    }

    private <T> void handle(StreamObserver<T> observer, Callable<T> action) {
        try {
            observer.onNext(action.call());
            observer.onCompleted();
        } catch (ChatAlreadyRegisteredException | LinkAlreadyTrackedException e) {
            observer.onError(
                    Status.ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException());
        } catch (ChatNotFoundException | LinkNotFoundException e) {
            observer.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (InvalidRequestException e) {
            observer.onError(
                    Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            observer.onError(Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }
}
