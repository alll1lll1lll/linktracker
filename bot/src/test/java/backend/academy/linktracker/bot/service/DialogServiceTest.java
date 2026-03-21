package backend.academy.linktracker.bot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.service.dialog.DialogService;
import backend.academy.linktracker.bot.service.dialog.DialogStageHandler;
import backend.academy.linktracker.bot.state.Context;
import backend.academy.linktracker.bot.state.State;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DialogServiceTest {

    @Mock
    private StateService stateService;

    @Mock
    private MessageService messageService;

    @Mock
    private DialogStageHandler linkHandler;

    @Mock
    private DialogStageHandler tagsHandler;

    private DialogService dialogService;

    private final long CHAT_ID = 12345L;
    private final String TEXT = "some input text";
    private final String HELP_MESSAGE = "please use /help";

    @BeforeEach
    void setUp() {
        dialogService = new DialogService(stateService, messageService, List.of(linkHandler, tagsHandler));
    }

    @Test
    @DisplayName("processDialogMessage: должен делегировать link handler'у, когда состояние WAITING_FOR_LINK")
    void processDialogMessage_delegatesToLinkHandler() {
        Context context = new Context();
        context.setState(State.WAITING_FOR_LINK);
        SendMessage expectedResponse = new SendMessage(CHAT_ID, "link accepted");

        when(stateService.get(CHAT_ID)).thenReturn(context);
        when(linkHandler.supports(State.WAITING_FOR_LINK)).thenReturn(true);
        when(linkHandler.handle(CHAT_ID, TEXT)).thenReturn(expectedResponse);

        SendMessage actualResponse = dialogService.processDialog(CHAT_ID, TEXT);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(linkHandler).handle(CHAT_ID, TEXT);
        verifyNoInteractions(tagsHandler);
    }

    @Test
    @DisplayName("processDialogMessage: должен делегировать tags handler'у, когда состояние WAITING_FOR_TAGS")
    void processDialogMessage_delegatesToTagsHandler() {
        Context context = new Context();
        context.setState(State.WAITING_FOR_TAGS);
        SendMessage expectedResponse = new SendMessage(CHAT_ID, "tags saved");

        when(stateService.get(CHAT_ID)).thenReturn(context);
        when(linkHandler.supports(State.WAITING_FOR_TAGS)).thenReturn(false);
        when(tagsHandler.supports(State.WAITING_FOR_TAGS)).thenReturn(true);
        when(tagsHandler.handle(CHAT_ID, TEXT)).thenReturn(expectedResponse);

        SendMessage actualResponse = dialogService.processDialog(CHAT_ID, TEXT);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(tagsHandler).handle(CHAT_ID, TEXT);
        verify(linkHandler).supports(State.WAITING_FOR_TAGS);
    }
}
