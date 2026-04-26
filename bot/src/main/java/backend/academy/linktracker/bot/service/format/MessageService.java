package backend.academy.linktracker.bot.service.format;

import backend.academy.linktracker.bot.enumResponse.ResponseCode;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageSource messageSource;

    public String getMessage(ResponseCode code, Object... args) {
        return messageSource.getMessage(code.getValue(), args, Locale.getDefault());
    }
}
