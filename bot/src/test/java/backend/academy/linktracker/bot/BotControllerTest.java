package backend.academy.linktracker.bot;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.bot.service.ListenerService;
import com.pengrad.telegrambot.TelegramBot;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class BotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TelegramBot telegramBot;

    @MockitoBean(answers = Answers.RETURNS_MOCKS)
    private ListenerService listenerService;

    @Test
    void test1() throws Exception {
        String validJson = """
                {
                  "id": 1,
                  "url": "https://github.com/user/repo",
                  "description": "обновление",
                  "tgChatIds": [123, 456]
                }
                """;

        mockMvc.perform(post("/updates").contentType(MediaType.APPLICATION_JSON).content(validJson))
                .andExpect(status().isOk());
    }

    @Test
    void test2() throws Exception {
        String invalidJson = """
                {
                  "id": "",
                  "url": "sdpe2swof[dw"
                }""";

        mockMvc.perform(post("/updates").contentType(MediaType.APPLICATION_JSON).content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
