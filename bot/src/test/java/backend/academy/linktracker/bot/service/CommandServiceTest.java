package backend.academy.linktracker.bot.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.commands.Command;
import backend.academy.linktracker.bot.commands.HelpCommand;
import backend.academy.linktracker.bot.commands.StartCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommandServiceTest {

    @Mock
    private StartCommand startCommand;

    @Mock
    private HelpCommand helpCommand;

    private CommandService commandService;

    @BeforeEach
    void setUp() {
        when(startCommand.getCommandName()).thenReturn("/start");
        when(helpCommand.getCommandName()).thenReturn("/help");
        List<Command> commands = Arrays.asList(startCommand, helpCommand);
        commandService = new CommandService(commands);
    }

    @Test
    void shouldProcessStartCommand() {
        Update update = mock(Update.class);
        long chatId = 1L;
        String commandName = "/start";
        when(startCommand.handle(update, chatId, commandName)).thenReturn(new SendMessage(chatId, "OK"));
        SendMessage result = commandService.processCommand(commandName, update, chatId);
        Assertions.assertNotNull(result);
        verify(startCommand).handle(update, chatId, commandName);
        verify(helpCommand, Mockito.never()).handle(any(), anyLong(), anyString());
    }

    @Test
    void shouldReturnUnknownCommandMessage() {
        Update update = mock(Update.class);
        SendMessage result = commandService.processCommand("/unknown", update, 1L);
        String text = (String) result.getParameters().get("text");
        Assertions.assertEquals("Неизвестная команда, используйте /help", text);
        verify(startCommand, Mockito.never()).handle(any(), anyLong(), anyString());
        verify(helpCommand, Mockito.never()).handle(any(), anyLong(), anyString());
    }
}
