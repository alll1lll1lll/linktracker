package backend.academy.linktracker.bot.state;

import java.net.URI;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Context {
    public State state = State.DEFAULT;
    public URI pendingUrl;
}
