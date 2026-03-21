package backend.academy.linktracker.bot.exception;

import java.io.Serial;

public class ServiceUnavailableException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public ServiceUnavailableException(String message) {
        super(message);
    }
}
