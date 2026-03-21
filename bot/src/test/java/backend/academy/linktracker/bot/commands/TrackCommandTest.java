package backend.academy.linktracker.bot.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.service.StateService;
import backend.academy.linktracker.bot.state.Context;
import backend.academy.linktracker.bot.state.State;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrackCommandTest {

    @Mock
    private StateService stateService;

    @Mock
    private Update update;

    @InjectMocks
    private TrackCommand trackCommand;

    private final long chatId = 100L;

    @Test
    void shouldChangeStateToWaitingForLink() {
        Context context = new Context();
        context.state = State.DEFAULT;

        when(stateService.get(chatId)).thenReturn(context);

        SendMessage response = trackCommand.handle(update, chatId, "/track");

        assertThat(context.state).isEqualTo(State.WAITING_FOR_LINK);

        String actualText = (String) response.getParameters().get("text");
        assertThat(actualText).isEqualTo("введите ссылку для отслеживания:");
    }
}
