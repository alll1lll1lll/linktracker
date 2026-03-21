package backend.academy.linktracker.scrapper.exception;

import java.io.Serial;

public class ChatAlreadyRegisteredException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public ChatAlreadyRegisteredException(long chatId) {
        super("чат с id " + chatId + " уже зарегистрирован");
    }
}
