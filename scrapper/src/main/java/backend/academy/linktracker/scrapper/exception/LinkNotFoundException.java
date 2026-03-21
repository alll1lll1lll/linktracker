package backend.academy.linktracker.scrapper.exception;

import java.io.Serial;

public class LinkNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public LinkNotFoundException(String url) {
        super("ссылка " + url + " не найдена");
    }
}
