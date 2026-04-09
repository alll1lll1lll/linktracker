package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.model.LinkModel;
import java.util.List;
import java.util.Map;

public interface SubscriptionRepository {
    void subscribe(long chatId, long linkId, List<String> tags);

    void unsubscribe(long chatId, long linkId);

    boolean isSubscribed(long chatId, long linkId);

    List<LinkModel> findAllByChatId(long chatId);

    List<Long> findChatSubscribers(long linkId);

    Map<Long, List<String>> findAllTagsByChatId(long chatId);
}
