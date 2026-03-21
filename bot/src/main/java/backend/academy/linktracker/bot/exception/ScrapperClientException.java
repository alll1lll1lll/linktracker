package backend.academy.linktracker.bot.exception;

import java.io.Serial;

public class ScrapperClientException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public ScrapperClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
