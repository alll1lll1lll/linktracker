package backend.academy.linktracker.bot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.exception.ServiceUnavailableException;
import backend.academy.linktracker.bot.service.routing.UpdateRouter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class BotIntegrationTest {

    @Autowired
    private UpdateRouter updateRouter;

    @MockitoBean
    private ScrapperClient scrapperClient;

    @MockitoBean(answers = Answers.RETURNS_MOCKS)
    private TelegramBot telegramBot;

    private Update createMockUpdate(long chatId, String text) {
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);

        Mockito.when(update.message()).thenReturn(message);
        Mockito.when(message.chat()).thenReturn(chat);
        Mockito.when(chat.id()).thenReturn(chatId);
        Mockito.when(message.text()).thenReturn(text);
        return update;
    }

    @Test
    @DisplayName("user registration via /start command")
    void scenario_registerUserSuccess() {
        long chatId = 12345L;
        Update update = createMockUpdate(chatId, "/start");

        Mockito.doNothing().when(scrapperClient).register(anyLong());

        SendMessage response = updateRouter.route(update);

        assertThat(response.getParameters().get("text").toString()).contains("вы успешно зарегистрированы");

        Mockito.verify(scrapperClient, Mockito.times(1)).register(chatId);
    }

    @Test
    @DisplayName("list links when scrapper returns links")
    void scenario_listLinksSuccess() throws Exception {
        long chatId = 777L;
        Update update = createMockUpdate(chatId, "/list");

        String json = """
                {
                  "links": [
                    {"id": 1, "url": "https://github.com/project"},
                    {"id": 2, "url": "https://stackoverflow.com/q/1"}
                  ],
                  "size": 2
                }
                """;

        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

        Method getLinksMethod = Arrays.stream(ScrapperClient.class.getMethods())
                .filter(m -> m.getName().equals("getLinks"))
                .findFirst()
                .orElseThrow();

        Object responseDto =
                objectMapper.readValue(json, objectMapper.constructType(getLinksMethod.getGenericReturnType()));

        Mockito.doReturn(responseDto).when(scrapperClient).getLinks(anyLong());

        SendMessage response = updateRouter.route(update);

        String responseText = response.getParameters().get("text").toString();
        assertThat(responseText).contains("https://github.com/project");
        assertThat(responseText).contains("https://stackoverflow.com/q/1");
    }

    @Test
    @DisplayName("scrapper is unavailable")
    void scenario_scrapperError() {
        long chatId = 999L;
        Update update = createMockUpdate(chatId, "/start");

        Mockito.doThrow(new ServiceUnavailableException("scrapper grpc service unavailable"))
                .when(scrapperClient)
                .register(anyLong());

        SendMessage response = updateRouter.route(update);

        assertThat(response.getParameters().get("text").toString()).contains("сервис временно недоступен");
    }
}
