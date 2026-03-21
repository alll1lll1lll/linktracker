package backend.academy.linktracker.bot.exception;

import java.io.Serial;

public class ChatAlreadyExistsException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public ChatAlreadyExistsException(String message) {
        super(message);
    }
}
