package backend.academy.linktracker.scrapper.exception.handler;

import backend.academy.linktracker.scrapper.dto.response.ApiErrorResponse;
import backend.academy.linktracker.scrapper.exception.ChatAlreadyRegisteredException;
import backend.academy.linktracker.scrapper.exception.ChatNotFoundException;
import backend.academy.linktracker.scrapper.exception.InvalidRequestException;
import backend.academy.linktracker.scrapper.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.exception.LinkNotFoundException;
import backend.academy.linktracker.scrapper.exception.LinkNotFoundForUserException;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ScrapperExceptionHandler {

    @ExceptionHandler(ChatNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleChatNotFound(ChatNotFoundException ex) {
        return buildErrorResponse(ex, "чат не найден", "404");
    }

    @ExceptionHandler(ChatAlreadyRegisteredException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleChatAlreadyExists(ChatAlreadyRegisteredException ex) {
        return buildErrorResponse(ex, "чат уже существует", "409");
    }

    @ExceptionHandler(LinkNotFoundForUserException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleLinkNotFoundForUser(LinkNotFoundForUserException ex) {
        return buildErrorResponse(ex, "ссылка для юзера не найдена", "409");
    }

    @ExceptionHandler(LinkNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleLinkNotFound(LinkNotFoundException ex) {
        return buildErrorResponse(ex, "ссылка не найдена", "409");
    }

    @ExceptionHandler(LinkAlreadyTrackedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleLinkAlreadyTracked(LinkAlreadyTrackedException ex) {
        return buildErrorResponse(ex, "ссылка уже отслеживается", "409");
    }

    @ExceptionHandler(InvalidRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleBadRequest(InvalidRequestException ex) {
        return buildErrorResponse(ex, "некорректные параметры запроса", "400");
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
