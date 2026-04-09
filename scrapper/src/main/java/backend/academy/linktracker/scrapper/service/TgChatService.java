package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.exception.ChatAlreadyRegisteredException;
import backend.academy.linktracker.scrapper.exception.ChatNotFoundException;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TgChatService {
    private final ChatRepository chatRepository;

    @Transactional
    public void register(long chatId) {
        if (chatRepository.exists(chatId)) {
            throw new ChatAlreadyRegisteredException(chatId);
        }
        chatRepository.add(chatId);
    }

    @Transactional
    public void deleteChat(long chatId) {
        if (!chatRepository.exists(chatId)) {
            throw new ChatNotFoundException(chatId);
        }
        chatRepository.remove(chatId);
    }
}
