package backend.academy.linktracker.bot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.commands.Command;
import backend.academy.linktracker.bot.commands.CommandType;
import backend.academy.linktracker.bot.enumResponse.ResponseCode;
import backend.academy.linktracker.bot.service.format.MessageService;
import backend.academy.linktracker.bot.service.parser.CommandParser;
import backend.academy.linktracker.bot.service.routing.CommandService;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommandServiceTest {

    @Mock
    private MessageService messageService;

    @Mock
    private CommandParser commandParser;

    @Mock
    private Command mockCommand;

    @Mock
    private Update update;

    private CommandService commandService;

    private final long CHAT_ID = 12345L;
    private final String UNKNOWN_COMMAND_MESSAGE = "Unknown command";
    private final String START_COMMAND_NAME = "/start";

    @BeforeEach
    void setUp() {
        when(mockCommand.getCommandType()).thenReturn(CommandType.START);

        commandService = new CommandService(List.of(mockCommand), messageService, commandParser);
    }

    @Test
    @DisplayName("процесс обработки известной команды без аргументов")
    void process_shouldExecuteKnownCommand_whenNoArgumentsExpected() {
        String fullText = START_COMMAND_NAME;
        SendMessage expectedResponse = new SendMessage(CHAT_ID, "Success");

        when(commandParser.hasArguments(fullText)).thenReturn(false);
        when(mockCommand.handle(update, CHAT_ID, fullText)).thenReturn(expectedResponse);

        SendMessage actualResponse = commandService.process(START_COMMAND_NAME, fullText, update, CHAT_ID);

        assertEquals(expectedResponse, actualResponse);
        verify(mockCommand).handle(update, CHAT_ID, fullText);
    }

    @Test
    @DisplayName("возвращает сообщение о неизвестной команде, если команда не найдена")
    void notFound() {
        String unknownCommand = "/unknown";
        when(messageService.getMessage(ResponseCode.UNKNOWN_COMMAND)).thenReturn(UNKNOWN_COMMAND_MESSAGE);

        SendMessage actualResponse = commandService.process(unknownCommand, unknownCommand, update, CHAT_ID);

        assertEquals(CHAT_ID, actualResponse.getParameters().get("chat_id"));
        assertEquals(UNKNOWN_COMMAND_MESSAGE, actualResponse.getParameters().get("text"));
        verify(mockCommand, never()).handle(any(), anyLong(), any());
    }

    @Test
    @DisplayName("возвращает сообщение о неизвестной команде, если переданы аргументы, а команда их не принимает")
    void argumentsProvidedButNotAccepted() {
        String fullTextWithArgs = START_COMMAND_NAME + " some_args";
        when(commandParser.hasArguments(fullTextWithArgs)).thenReturn(true);
        when(mockCommand.acceptsArguments()).thenReturn(false);
        when(messageService.getMessage(ResponseCode.UNKNOWN_COMMAND)).thenReturn(UNKNOWN_COMMAND_MESSAGE);

        SendMessage actualResponse = commandService.process(START_COMMAND_NAME, fullTextWithArgs, update, CHAT_ID);

        assertEquals(CHAT_ID, actualResponse.getParameters().get("chat_id"));
        assertEquals(UNKNOWN_COMMAND_MESSAGE, actualResponse.getParameters().get("text"));
        verify(mockCommand, never()).handle(any(), anyLong(), any());
    }

    @Test
    @DisplayName("Getter allCommandList возвращает правильный список команд")
    void getAllCommandList_shouldReturnCorrectList() {
        assertEquals(1, commandService.getAllCommandList().size());
        assertEquals(mockCommand, commandService.getAllCommandList().get(0));
    }
}
