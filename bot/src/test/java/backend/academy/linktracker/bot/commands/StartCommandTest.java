package backend.academy.linktracker.bot.commands;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.client.ScrapperClient;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StartCommandTest {

    @Test
    void shouldReturnWelcomeMessage() {
        ScrapperClient scrapperClient = mock(ScrapperClient.class);
        StartCommand command = new StartCommand(scrapperClient);

        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(123L);

        SendMessage result = command.handle(update, 123L, "/start");

        String text = (String) result.getParameters().get("text");
        Long chatId = (Long) result.getParameters().get("chat_id");

        Assertions.assertEquals(123L, chatId);
        Assertions.assertTrue(text.contains("добро пожаловать"));
    }
}
