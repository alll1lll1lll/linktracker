package backend.academy.linktracker.bot.enumResponse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseCode {
    UNKNOWN_COMMAND("UNKNOWN_COMMAND"),
    LINK_ACCEPTED("LINK_ACCEPTED"),
    INVALID_LINK_FORMAT("INVALID_LINK_FORMAT"),
    CANCELLED("CANCELLED"),
    PLEASE_USE_HELP("PLEASE_USE_HELP"),
    ALREADY_TRACKED("ALREADY_TRACKED"),
    TRACK_SUCCESS("TRACK_SUCCESS"),
    UNTRACK_SUCCESS("UNTRACK_SUCCESS"),
    NOT_FOUND("NOT_FOUND"),
    ERROR_ADD("ERROR_ADD"),
    ERROR_UNTRACK("ERROR_UNTRACK");

    private final String value;
}
