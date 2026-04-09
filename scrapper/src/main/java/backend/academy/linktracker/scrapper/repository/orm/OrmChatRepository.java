package backend.academy.linktracker.scrapper.repository.orm;

import backend.academy.linktracker.scrapper.entity.ChatEntity;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrmChatRepository implements ChatRepository {

    private final EntityManager entityManager;

    @Override
    public void add(long chatId) {
        if (!exists(chatId)) {
            ChatEntity chat = new ChatEntity();
            chat.setId(chatId);
            entityManager.persist(chat);
        }
    }

    @Override
    public void remove(long chatId) {
        ChatEntity chat = entityManager.find(ChatEntity.class, chatId);
        if (chat != null) {
            entityManager.remove(chat);
        }
    }

    @Override
    public boolean exists(long chatId) {
        return entityManager.find(ChatEntity.class, chatId) != null;
    }
}
