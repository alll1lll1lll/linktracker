package backend.academy.linktracker.scrapper.controller;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.scrapper.service.TgChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TgChatController.class)
class TgChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TgChatService tgChatService;

    @Test
    void registerChat_ShouldReturnOk() throws Exception {
        mockMvc.perform(post("/tg-chat/123")).andExpect(status().isOk());
        verify(tgChatService).register(123L);
    }

    @Test
    void deleteChat_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/tg-chat/123")).andExpect(status().isOk());

        verify(tgChatService).deleteChat(123L);
    }
}
