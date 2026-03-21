package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.service.TgChatService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/tg-chat")
public class TgChatController {
    private final TgChatService tgChatService;

    @PostMapping("/{id}")
    public void register(@PathVariable("id") long id) {
        tgChatService.register(id);
    }

    @DeleteMapping("/{id}")
    public void deleteChat(@PathVariable("id") long id) {
        tgChatService.deleteChat(id);
    }
}
