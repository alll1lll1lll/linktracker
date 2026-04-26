package backend.academy.linktracker.bot.service.dialog;

import backend.academy.linktracker.bot.state.Context;
import backend.academy.linktracker.bot.state.State;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StateService {
    private final Map<Long, Context> states = new ConcurrentHashMap<>();

    public Context get(long chatId) {
        return states.computeIfAbsent(chatId, k -> new Context());
    }

    public boolean isInDialog(long chatId) {
        return get(chatId).getState() != State.DEFAULT;
    }

    public void updateContext(long chatId, Consumer<Context> action) {
        states.compute(chatId, (k, context) -> {
            Context ctx = (context == null) ? new Context() : context;
            action.accept(ctx);
            return ctx;
        });
    }

    public void reset(long chatId) {
        if (states.remove(chatId) != null) {
            log.atInfo().addKeyValue("event", "state_reset").log("State has been reset for user {}", chatId);
        }
    }
}
