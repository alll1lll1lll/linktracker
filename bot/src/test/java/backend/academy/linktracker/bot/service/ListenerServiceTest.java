package backend.academy.linktracker.bot.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pengrad.telegrambot.ExceptionHandler;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListenerServiceTest {

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private CommandService commandService;

    @InjectMocks
    private ListenerService listenerService;

    @Mock
    private BaseResponse baseResponse;

    @Mock
    private SendResponse sendResponse;

    @BeforeEach
    void setUp() {
        when(baseResponse.isOk()).thenReturn(true);
        when(telegramBot.execute(any(SetMyCommands.class))).thenReturn(baseResponse);
        when(commandService.getAllCommandList()).thenReturn(Collections.emptyList());
    }

    @Test
    void shouldRegisterListenerOnRun() {
        listenerService.run();
        verify(telegramBot).setUpdatesListener(any(), any(ExceptionHandler.class));
        verify(telegramBot).execute(any(SetMyCommands.class));
    }

    @Test
    void shouldProcessCommandUpdate() {
        long chatId = 100L;
        String commandText = "/start";

        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        SendMessage expectedRequest = new SendMessage(chatId, "command response");

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(message.text()).thenReturn(commandText);
        when(chat.id()).thenReturn(chatId);

        when(commandService.processCommand(eq(commandText), eq(update), eq(chatId)))
                .thenReturn(expectedRequest);
        when(telegramBot.execute(expectedRequest)).thenReturn(sendResponse);

        listenerService.run();
        ArgumentCaptor<UpdatesListener> listenerCaptor = ArgumentCaptor.forClass(UpdatesListener.class);
        verify(telegramBot).setUpdatesListener(listenerCaptor.capture(), any(ExceptionHandler.class));
        UpdatesListener capturedListener = listenerCaptor.getValue();

        capturedListener.process(List.of(update));
        verify(commandService).processCommand(eq("/start"), eq(update), eq(100L));
        verify(telegramBot).execute(expectedRequest);
    }

    @Test
    void shouldIgnoreInvalidUpdates() {
        Update updateNullMessage = mock(Update.class);
        when(updateNullMessage.message()).thenReturn(null);
        Update updateNullText = mock(Update.class);
        Message message = mock(Message.class);
        when(updateNullText.message()).thenReturn(message);
        when(message.text()).thenReturn(null);

        listenerService.run();
        ArgumentCaptor<UpdatesListener> listenerCaptor = ArgumentCaptor.forClass(UpdatesListener.class);
        verify(telegramBot).setUpdatesListener(listenerCaptor.capture(), any(ExceptionHandler.class));
        UpdatesListener capturedListener = listenerCaptor.getValue();

        capturedListener.process(List.of(updateNullMessage, updateNullText));
        verify(commandService, never()).processCommand(any(), any(), anyLong());
        verify(telegramBot, never()).execute(any(SendMessage.class));
    }
}
