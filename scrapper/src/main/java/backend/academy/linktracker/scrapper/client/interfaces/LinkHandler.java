package backend.academy.linktracker.scrapper.client.interfaces;

import backend.academy.linktracker.scrapper.model.LinkModel;
import java.net.URI;

public interface LinkHandler {
    String getHost();

    void handle(LinkModel linkModel);

    default boolean supports(URI uri) {
        return uri != null && getHost().equalsIgnoreCase(uri.getHost());
    }
}
