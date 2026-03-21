package backend.academy.linktracker.bot.service;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.bot.state.Context;
import backend.academy.linktracker.bot.state.State;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StateServiceTest {

    private final StateService stateService = new StateService();
    private final long TEST_CHAT_ID = 12345L;

    @Test
    @DisplayName("получение контекста для нового чата должно возвращать дефолтный контекст")
    void get_newChat_returnsDefaultContext() {

        Context context = stateService.get(TEST_CHAT_ID);

        assertThat(context).isNotNull();
        assertThat(context.getState()).isEqualTo(State.DEFAULT);
        assertThat(context.getPendingUrl()).isNull();
    }

    @Test
    @DisplayName("получение контекста для существующего чата должно возвращать сохраненный контекст")
    void get_existingChat_returnsSavedContext() throws URISyntaxException {
        URI testUri = new URI("http://example.com");
        stateService.updateContext(TEST_CHAT_ID, context -> {
            context.setState(State.WAITING_FOR_LINK);
            context.setPendingUrl(testUri);
        });

        Context context = stateService.get(TEST_CHAT_ID);

        assertThat(context).isNotNull();
        assertThat(context.getState()).isEqualTo(State.WAITING_FOR_LINK);
        assertThat(context.getPendingUrl()).isEqualTo(testUri);
    }

    @Test
    @DisplayName("isInDialog должен возвращать true, если состояние не DEFAULT")
    void isInDialog_notDefaultState_returnsTrue() {
        stateService.updateContext(TEST_CHAT_ID, context -> context.setState(State.WAITING_FOR_LINK));

        boolean inDialog = stateService.isInDialog(TEST_CHAT_ID);

        assertThat(inDialog).isTrue();
    }

    @Test
    @DisplayName("isInDialog должен возвращать false, если состояние DEFAULT")
    void isInDialog_defaultState_returnsFalse() {
        stateService.updateContext(TEST_CHAT_ID, context -> context.setState(State.DEFAULT));

        boolean inDialog = stateService.isInDialog(TEST_CHAT_ID);

        assertThat(inDialog).isFalse();
    }

    @Test
    @DisplayName("updateContext должен корректно обновлять существующий контекст")
    void updateContext_existingContext_updatesCorrectly() throws URISyntaxException {
        URI initialUri = new URI("http://initial.com");
        stateService.updateContext(TEST_CHAT_ID, context -> {
            context.setState(State.DEFAULT);
            context.setPendingUrl(initialUri);
        });

        URI newUri = new URI("http://new.com");

        stateService.updateContext(TEST_CHAT_ID, context -> {
            context.setState(State.WAITING_FOR_TAGS);
            context.setPendingUrl(newUri);
        });

        Context updatedContext = stateService.get(TEST_CHAT_ID);
        assertThat(updatedContext.getState()).isEqualTo(State.WAITING_FOR_TAGS);
        assertThat(updatedContext.getPendingUrl()).isEqualTo(newUri);
    }

    @Test
    @DisplayName("reset должен удалять состояние для чата")
    void reset_existingChat_removesState() {
        stateService.updateContext(TEST_CHAT_ID, context -> context.setState(State.WAITING_FOR_LINK));
        assertThat(stateService.isInDialog(TEST_CHAT_ID)).isTrue();

        stateService.reset(TEST_CHAT_ID);

        assertThat(stateService.isInDialog(TEST_CHAT_ID)).isFalse();
        assertThat(stateService.get(TEST_CHAT_ID).getState()).isEqualTo(State.DEFAULT);
    }

    @Test
    @DisplayName("reset не должен влиять на другие чаты")
    void reset_otherChatsUnaffected() {
        long otherChatId = 67890L;
        stateService.updateContext(TEST_CHAT_ID, context -> context.setState(State.WAITING_FOR_LINK));
        stateService.updateContext(otherChatId, context -> context.setState(State.WAITING_FOR_TAGS));

        stateService.reset(TEST_CHAT_ID);

        assertThat(stateService.isInDialog(TEST_CHAT_ID)).isFalse();
        assertThat(stateService.isInDialog(otherChatId)).isTrue();
        assertThat(stateService.get(otherChatId).getState()).isEqualTo(State.WAITING_FOR_TAGS);
    }

    @Test
    @DisplayName("многопоточное обновление контекста должно быть безопасным")
    void updateContext_concurrently_isThreadSafe() throws InterruptedException, URISyntaxException {
        int numberOfThreads = 100;
        var executor = Executors.newFixedThreadPool(10);
        URI finalUri = new URI("http://final.com");

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> stateService.updateContext(TEST_CHAT_ID, context -> {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                context.setState(State.WAITING_FOR_LINK);
                context.setPendingUrl(finalUri);
            }));
        }
        executor.shutdown();
        boolean finished = executor.awaitTermination(10, TimeUnit.SECONDS);

        assertThat(finished).isTrue();
        Context finalContext = stateService.get(TEST_CHAT_ID);
        assertThat(finalContext.getState()).isEqualTo(State.WAITING_FOR_LINK);
        assertThat(finalContext.getPendingUrl()).isEqualTo(finalUri);
    }
}
