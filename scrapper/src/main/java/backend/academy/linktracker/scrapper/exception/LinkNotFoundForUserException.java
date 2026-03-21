package backend.academy.linktracker.scrapper.exception;

import java.io.Serial;

public class LinkNotFoundForUserException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public LinkNotFoundForUserException(String message) {
        super(message);
    }
}
