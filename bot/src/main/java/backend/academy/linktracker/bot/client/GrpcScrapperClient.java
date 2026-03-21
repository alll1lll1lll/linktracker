package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.bot.dto.*;
import backend.academy.linktracker.bot.exception.*;
import backend.academy.linktracker.bot.mapper.GrpcMapper;
import backend.academy.linktracker.grpc.ScrapperServiceGrpc;
import io.grpc.ClientInterceptor;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.net.URI;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class GrpcScrapperClient implements ScrapperClient {

    private final ScrapperServiceGrpc.ScrapperServiceBlockingStub scrapperStub;
    private final GrpcMapper mapper;

    @Override
    public void register(long chatId) {
        try {
            scrapperStub.registerChat(mapper.toChatIdRequest(chatId));
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.ALREADY_EXISTS) {
                log.warn("chatid {} already registered in scrapper via grpc", chatId);
                throw new ChatAlreadyExistsException("chat already registered");
            }
            log.error("grpc server error during registration for chatid {}: {}", chatId, e.getStatus());
            throw new ServiceUnavailableException("scrapper grpc service unavailable");
        }
    }

    @Override
    public void delete(long chatId) {
        try {
            scrapperStub.deleteChat(mapper.toChatIdRequest(chatId));
        } catch (StatusRuntimeException e) {
            log.error("grpc server error during chat deletion for chatid {}", chatId, e);
            throw new ServiceUnavailableException("scrapper grpc service unavailable");
        }
    }

    @Override
    public void addLink(long chatId, AddLinkRequest request) {
        try {
            ClientInterceptor interceptor = new ChatIdClientInterceptor(chatId);
            scrapperStub.withInterceptors(interceptor).addLink(mapper.toAddLinkMsg(request));
        } catch (StatusRuntimeException e) {
            Status.Code code = e.getStatus().getCode();
            if (code == Status.Code.ALREADY_EXISTS) {
                log.warn("link {} already tracked for chatid {} (grpc)", request.getLink(), chatId);
                throw new LinkAlreadyExistsException("link already tracked");
            }
            if (code == Status.Code.INVALID_ARGUMENT) {
                log.warn(
                        "invalid link arguments for chatid {}: {}",
                        chatId,
                        e.getStatus().getDescription());
                throw new ScrapperClientException("invalid request to scrapper", e);
            }
            log.error("grpc server error while adding link for chatid {}", chatId, e);
            throw new ServiceUnavailableException("scrapper grpc service unavailable");
        }
    }

    @Override
    public void removeLink(long chatId, RemoveLinkRequest request) {
        try {
            ClientInterceptor interceptor = new ChatIdClientInterceptor(chatId);
            scrapperStub.withInterceptors(interceptor).removeLink(mapper.toRemoveLinkMsg(request));
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                log.warn("link {} not found for chatid {} (grpc)", request.getLink(), chatId);
                throw new LinkNotFoundException("link not found");
            }
            log.error("grpc server error while removing link for chatid {}", chatId, e);
            throw new ServiceUnavailableException("scrapper grpc service unavailable");
        }
    }

    @Override
    public ListLinksResponse getLinks(long chatId) {
        try {
            ClientInterceptor interceptor = new ChatIdClientInterceptor(chatId);

            var res = scrapperStub.withInterceptors(interceptor).getLinks(mapper.toChatIdRequest(chatId));
            var links = res.getLinksList().stream()
                    .map(l -> new LinkResponse(l.getId(), URI.create(l.getUrl()), l.getTagsList()))
                    .collect(Collectors.toList());
            return new ListLinksResponse(links, res.getSize());
        } catch (StatusRuntimeException e) {
            log.error("grpc server error while getting links for chatid {}", chatId, e);
            throw new ServiceUnavailableException("scrapper grpc service unavailable");
        }
    }
}
