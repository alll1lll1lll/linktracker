package backend.academy.linktracker.scrapper.exception;

import java.io.Serial;

public class LinkAlreadyTrackedException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public LinkAlreadyTrackedException(long chatId, String url) {
        super("cсылка " + url + " уже отслеживается в чате " + chatId);
    }
}
