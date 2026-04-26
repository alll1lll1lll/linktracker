package backend.academy.linktracker.bot.service.tracking;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.dto.AddLinkRequest;
import backend.academy.linktracker.bot.dto.RemoveLinkRequest;
import backend.academy.linktracker.bot.enumResponse.ResponseCode;
import backend.academy.linktracker.bot.exception.LinkAlreadyExistsException;
import backend.academy.linktracker.bot.exception.LinkNotFoundException;
import backend.academy.linktracker.bot.exception.ServiceUnavailableException;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackService {
    private final ScrapperClient scrapperClient;

    public ResponseCode track(long chatId, URI url, List<String> tags) {
        try {
            scrapperClient.addLink(chatId, new AddLinkRequest(url, tags));
            log.info("Link {} tracked for user {}", url, chatId);
            return ResponseCode.TRACK_SUCCESS;
        } catch (LinkAlreadyExistsException e) {
            log.warn("Link {} already tracked for user {}", url, chatId);
            return ResponseCode.ALREADY_TRACKED;
        } catch (ServiceUnavailableException e) {
            log.error("Scrapper service unavailable for user {}", chatId, e);
            return ResponseCode.ERROR_ADD;
        } catch (Exception e) {
            log.error("Unexpected error tracking link for user {}", chatId, e);
            return ResponseCode.ERROR_ADD;
        }
    }

    public ResponseCode untrack(long chatId, String urlText) {
        try {
            URI url = new URI(urlText);
            scrapperClient.removeLink(chatId, new RemoveLinkRequest(url));
            log.info("Link {} untracked for user {}", url, chatId);
            return ResponseCode.UNTRACK_SUCCESS;
        } catch (LinkNotFoundException e) {
            log.warn("Link {} not found for user {}", urlText, chatId);
            return ResponseCode.NOT_FOUND;
        } catch (ServiceUnavailableException e) {
            log.error("Scrapper service unavailable for user {}", chatId, e);
            return ResponseCode.ERROR_UNTRACK;
        } catch (Exception e) {
            log.error("Unexpected error untracking link for user {}", chatId, e);
            return ResponseCode.ERROR_UNTRACK;
        }
    }
}
