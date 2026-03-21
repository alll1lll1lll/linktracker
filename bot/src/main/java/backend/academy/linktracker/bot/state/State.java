package backend.academy.linktracker.bot.state;

import lombok.Getter;

@Getter
public enum State {
    DEFAULT,
    WAITING_FOR_LINK,
    WAITING_FOR_TAGS,
    WAITING_FOR_UNTRACK
}
