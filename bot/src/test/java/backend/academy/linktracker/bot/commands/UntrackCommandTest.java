package backend.academy.linktracker.bot.commands;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.service.dialog.StateService;
import backend.academy.linktracker.bot.state.Context;
import backend.academy.linktracker.bot.state.State;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UntrackCommandTest {
    @Mock
    private StateService stateService;

    @Mock
    private Update update;

    private UntrackCommand untrackCommand;

    private final long chatId = 123L;

    @BeforeEach
    void setUp() {
        untrackCommand = new UntrackCommand(stateService);
    }

    @Test
    @DisplayName("ожидание смены состояния")
    void changeState() {
        Context context = new Context();
        context.state = State.DEFAULT;

        when(stateService.get(chatId)).thenReturn(context);

        SendMessage response = untrackCommand.handle(update, chatId, "/untrack");

        assertThat(context.state).isEqualTo(State.WAITING_FOR_UNTRACK);

        String trueText = (String) response.getParameters().get("text");
        assertThat(trueText).isEqualTo("введите ссылку, которую хотите перестать отслеживать");
    }
}
