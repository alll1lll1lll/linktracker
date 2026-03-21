package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.bot.dto.*;
import backend.academy.linktracker.bot.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@RequiredArgsConstructor
public class RestScrapperClient implements ScrapperClient {
    private final RestClient restClient;

    @Override
    public void register(long chatId) {
        try {
            restClient.post().uri("/tg-chat/{id}", chatId).retrieve().toBodilessEntity();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                throw new ChatAlreadyExistsException("chat " + chatId + " already registered.");
            }
            log.error("client error during registration for chatid {}: {}", chatId, e.getMessage());
            throw new ScrapperClientException("invalid request to scrapper", e);
        } catch (HttpServerErrorException | ResourceAccessException e) {
            log.error("scrapper service is unavailable or returned server error for chatid {}", chatId, e);
            throw new ServiceUnavailableException("scrapper service unavailable");
        }
    }

    @Override
    public void delete(long chatId) {
        try {
            restClient.delete().uri("/tg-chat/{id}", chatId).retrieve().toBodilessEntity();
        } catch (RestClientException e) {
            throw new ServiceUnavailableException("failed to delete chat " + chatId);
        }
    }

    @Override
    public void addLink(long chatId, AddLinkRequest request) {
        try {
            restClient
                    .post()
                    .uri("/links")
                    .header("Tg-Chat-Id", String.valueOf(chatId))
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                log.warn("link {} already exists for chatid {}", request.getLink(), chatId);
                throw new LinkAlreadyExistsException("link already tracked");
            }
            log.error("client error while adding link for chatid {}: {}", chatId, e.getMessage());
            throw new ScrapperClientException("invalid add link request", e);
        } catch (HttpServerErrorException | ResourceAccessException e) {
            log.error("scrapper server error while adding link for chatid {}", chatId, e);
            throw new ServiceUnavailableException("scrapper service unavailable");
        }
    }

    @Override
    public void removeLink(long chatId, RemoveLinkRequest request) {
        try {
            restClient
                    .method(HttpMethod.DELETE)
                    .uri("/links")
                    .header("Tg-Chat-Id", String.valueOf(chatId))
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("link {} not found for chatid {}", request.getLink(), chatId);
                throw new LinkNotFoundException("link not found");
            }
            throw new ScrapperClientException("invalid remove link request", e);
        } catch (HttpServerErrorException | ResourceAccessException e) {
            log.error("scrapper server error while removing link for chatid {}", chatId, e);
            throw new ServiceUnavailableException("scrapper service unavailable");
        }
    }

    @Override
    public ListLinksResponse getLinks(long chatId) {
        try {
            return restClient
                    .get()
                    .uri("/links")
                    .header("Tg-Chat-Id", String.valueOf(chatId))
                    .retrieve()
                    .body(ListLinksResponse.class);
        } catch (HttpClientErrorException e) {
            throw new ScrapperClientException("client error while fetching links", e);
        } catch (HttpServerErrorException | ResourceAccessException e) {
            log.error("failed to get links for chatid {} due to scrapper unavailability", chatId, e);
            throw new ServiceUnavailableException("scrapper service unavailable");
        }
    }
}
