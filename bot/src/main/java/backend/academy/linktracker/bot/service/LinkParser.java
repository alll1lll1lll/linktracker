package backend.academy.linktracker.bot.service;

import java.net.URI;
import java.net.URISyntaxException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkParser {

    public URI parse(String url) throws URISyntaxException {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();

            if (scheme == null || !(scheme.equals("http") || scheme.equals("https")) || uri.getHost() == null) {
                throw new URISyntaxException(url, "unsupported scheme or missing host");
            }

            return uri;
        } catch (URISyntaxException e) {
            log.warn("failed to parse url {}: {}", url, e.getMessage());
            throw e;
        }
    }
}
