package backend.academy.linktracker.bot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.enumResponse.ResponseCode;
import backend.academy.linktracker.bot.service.dialog.DialogService;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateRouterTest {

    @Mock
    private CommandService commandService;

    @Mock
    private DialogService dialogService;

    @Mock
    private StateService stateService;

    @Mock
    private MessageService messageService;

    @Mock
    private CommandParser commandParser;

    @InjectMocks
    private UpdateRouter updateRouter;

    private final long CHAT_ID = 12345L;
    private final String PLEASE_USE_HELP_MESSAGE = "Пожалуйста, используйте /help для получения информации о командах.";

    private Update createMockUpdate(String text) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(CHAT_ID);
        when(message.text()).thenReturn(text);
        return update;
    }

    @Test
    @DisplayName("route: команда в режиме диалога (не /cancel) должна сбросить состояние и обработать команду")
    void route_commandInDialog_notCancel_resetsStateAndProcessesCommand() {
        String commandText = "/start";
        Update update = createMockUpdate(commandText);
        SendMessage expectedResponse = new SendMessage(CHAT_ID, "Welcome!");

        when(commandParser.extractCommandName(commandText)).thenReturn("/start");
        when(stateService.isInDialog(CHAT_ID)).thenReturn(true);
        when(commandService.process(eq("/start"), eq(commandText), eq(update), eq(CHAT_ID)))
                .thenReturn(expectedResponse);

        SendMessage actualResponse = updateRouter.route(update);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(stateService).isInDialog(CHAT_ID);
        verify(stateService).reset(CHAT_ID);
        verify(commandService).process(eq("/start"), eq(commandText), eq(update), eq(CHAT_ID));
        verify(dialogService, never()).processDialog(anyLong(), any());
    }

    @Test
    @DisplayName("route: команда /cancel в режиме диалога должна обработать команду без сброса состояния")
    void route_cancelCommandInDialog_processesCommandWithoutReset() {
        String commandText = "/cancel";
        Update update = createMockUpdate(commandText);
        SendMessage expectedResponse = new SendMessage(CHAT_ID, "Canceled.");

        when(commandParser.extractCommandName(commandText)).thenReturn("/cancel");
        when(stateService.isInDialog(CHAT_ID)).thenReturn(true);
        when(commandService.process(eq("/cancel"), eq(commandText), eq(update), eq(CHAT_ID)))
                .thenReturn(expectedResponse);

        SendMessage actualResponse = updateRouter.route(update);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(stateService).isInDialog(CHAT_ID);
        verify(stateService, never()).reset(CHAT_ID);
        verify(commandService).process(eq("/cancel"), eq(commandText), eq(update), eq(CHAT_ID));
        verify(dialogService, never()).processDialog(anyLong(), any());
    }

    @Test
    @DisplayName("route: команда вне режима диалога должна быть обработана commandService")
    void route_commandNotInDialog_processesCommand() {
        String commandText = "/help";
        Update update = createMockUpdate(commandText);
        SendMessage expectedResponse = new SendMessage(CHAT_ID, "Help text.");

        when(commandParser.extractCommandName(commandText)).thenReturn("/help");
        when(stateService.isInDialog(CHAT_ID)).thenReturn(false);
        when(commandService.process(eq("/help"), eq(commandText), eq(update), eq(CHAT_ID)))
                .thenReturn(expectedResponse);

        SendMessage actualResponse = updateRouter.route(update);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(stateService).isInDialog(CHAT_ID);
        verify(stateService, never()).reset(CHAT_ID);
        verify(commandService).process(eq("/help"), eq(commandText), eq(update), eq(CHAT_ID));
        verify(dialogService, never()).processDialog(anyLong(), any());
    }

    @Test
    @DisplayName("route: обычное сообщение в режиме диалога должно быть обработано dialogService")
    void route_plainMessageInDialog_processesByDialogService() {
        String plainText = "some user input";
        Update update = createMockUpdate(plainText);
        SendMessage expectedResponse = new SendMessage(CHAT_ID, "Dialog response.");

        when(stateService.isInDialog(CHAT_ID)).thenReturn(true);
        when(dialogService.processDialog(eq(CHAT_ID), eq(plainText))).thenReturn(expectedResponse);

        SendMessage actualResponse = updateRouter.route(update);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(stateService).isInDialog(CHAT_ID);
        verify(commandService, never()).process(any(), any(), any(), anyLong());
        verify(dialogService).processDialog(eq(CHAT_ID), eq(plainText));
        verify(stateService, never()).reset(CHAT_ID);
    }

    @Test
    @DisplayName("route: обычное сообщение вне режима диалога должно получить сообщение HELP")
    void route_plainMessageNotInDialog_returnsHelpMessage() {
        String plainText = "some random text";
        Update update = createMockUpdate(plainText);

        when(stateService.isInDialog(CHAT_ID)).thenReturn(false);
        when(messageService.getMessage(ResponseCode.PLEASE_USE_HELP)).thenReturn(PLEASE_USE_HELP_MESSAGE);

        SendMessage actualResponse = updateRouter.route(update);

        assertThat(actualResponse.getParameters().get("chat_id")).isEqualTo(CHAT_ID);
        assertThat(actualResponse.getParameters().get("text")).isEqualTo(PLEASE_USE_HELP_MESSAGE);
        verify(stateService).isInDialog(CHAT_ID);
        verify(commandService, never()).process(any(), any(), any(), anyLong());
        verify(dialogService, never()).processDialog(anyLong(), any());
        verify(messageService).getMessage(ResponseCode.PLEASE_USE_HELP);
    }

    @Test
    @DisplayName("route: команда с пробелами в начале или конце должна быть обрезана")
    void route_trimmedCommand() {
        String commandText = "  /start   ";
        String trimmedCommandText = "/start";
        Update update = createMockUpdate(commandText);
        SendMessage expectedResponse = new SendMessage(CHAT_ID, "Welcome!");

        when(commandParser.extractCommandName(trimmedCommandText)).thenReturn(trimmedCommandText);
        when(stateService.isInDialog(CHAT_ID)).thenReturn(false);
        when(commandService.process(eq(trimmedCommandText), eq(trimmedCommandText), eq(update), eq(CHAT_ID)))
                .thenReturn(expectedResponse);

        SendMessage actualResponse = updateRouter.route(update);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(commandParser).extractCommandName(trimmedCommandText);
        verify(commandService).process(eq(trimmedCommandText), eq(trimmedCommandText), eq(update), eq(CHAT_ID));
    }

    @Test
    @DisplayName("route: обычное сообщение с пробелами в начале или конце должно быть обрезано")
    void route_trimmedPlainTextInDialog() {
        String plainTextWithSpaces = "  some input  ";
        String trimmedPlainText = "some input";
        Update update = createMockUpdate(plainTextWithSpaces);
        SendMessage expectedResponse = new SendMessage(CHAT_ID, "Dialog response.");

        when(stateService.isInDialog(CHAT_ID)).thenReturn(true);
        when(dialogService.processDialog(eq(CHAT_ID), eq(trimmedPlainText))).thenReturn(expectedResponse);

        SendMessage actualResponse = updateRouter.route(update);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(dialogService).processDialog(eq(CHAT_ID), eq(trimmedPlainText));
    }
}
