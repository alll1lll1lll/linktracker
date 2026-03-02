package backend.academy.linktracker.bot.commands;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HelpCommandTest {

    @Test
    void shouldReturnCommandsList() {
        List<Command> commandList = List.of(new StartCommand(), new HelpCommand(List.of()));

        HelpCommand helpCommand = new HelpCommand(commandList);

        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(123L);

        SendMessage result = helpCommand.handle(update, 123L, "/help");

        String text = (String) result.getParameters().get("text");

        Assertions.assertTrue(text.contains("Доступные команды"));
        Assertions.assertTrue(text.contains("/start"));
        Assertions.assertTrue(text.contains("/help"));
    }
}
