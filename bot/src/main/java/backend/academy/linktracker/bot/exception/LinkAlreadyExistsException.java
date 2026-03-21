package backend.academy.linktracker.bot.exception;

import java.io.Serial;

public class LinkAlreadyExistsException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public LinkAlreadyExistsException(String message) {
        super(message);
    }
}
