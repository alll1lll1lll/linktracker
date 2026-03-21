package backend.academy.linktracker.scrapper.repository;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class ChatRepository {
    private final Set<Long> registeredChats = ConcurrentHashMap.newKeySet();

    public void add(long chatId) {
        registeredChats.add(chatId);
    }

    public void remove(long chatId) {
        registeredChats.remove(chatId);
    }

    public boolean exists(long chatId) {
        return registeredChats.contains(chatId);
    }
}
