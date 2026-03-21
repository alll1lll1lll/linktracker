package backend.academy.linktracker.bot.exception.handler;

import backend.academy.linktracker.bot.dto.ApiErrorResponse;
import backend.academy.linktracker.bot.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.bot.exception.InvalidRequestException;
import backend.academy.linktracker.bot.exception.ServiceUnavailableException;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class BotExceptionHandler {
    @ExceptionHandler({InvalidRequestException.class, HttpMessageNotReadableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleBadRequest(Exception ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return buildErrorResponse(ex, "некорректные параметры запроса.", "400");
    }

    @ExceptionHandler(ChatAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleChatAlreadyExists(ChatAlreadyExistsException ex) {
        log.info("Conflict during request: {}", ex.getMessage());
        return buildErrorResponse(ex, "чат уже зарегистрирован.", "409");
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ApiErrorResponse handleServiceUnavailable(ServiceUnavailableException ex) {
        log.error("External service is unavailable.", ex);
        return buildErrorResponse(ex, "внешний сервис временно недоступен.", "503");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleGeneralException(Exception ex) {
        log.error("An internal server error occurred.", ex);
        return buildErrorResponse(ex, "внутренняя ошибка сервера.", "500");
    }

    private ApiErrorResponse buildErrorResponse(Exception ex, String description, String code) {
        List<String> stacktrace = Arrays.stream(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .toList();

        return ApiErrorResponse.builder()
                .description(description)
                .code(code)
                .exceptionName(ex.getClass().getName())
                .exceptionMessage(ex.getMessage())
                .stacktrace(stacktrace)
                .build();
    }
}
