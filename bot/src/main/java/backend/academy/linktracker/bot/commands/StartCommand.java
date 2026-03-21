package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.bot.exception.ServiceUnavailableException;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StartCommand extends Command {
    private final ScrapperClient scrapperClient;

    public StartCommand(ScrapperClient scrapperClient) {
        super(CommandType.START);
        this.scrapperClient = scrapperClient;
    }

    @Override
    public SendMessage handle(Update update, long chatId, String text) {
        try {
            scrapperClient.register(chatId);
            log.info("user with chatid {} successfully registered", chatId);
            return new SendMessage(chatId, "добро пожаловать! вы успешно зарегистрированы. используйте команду /help");

        } catch (ChatAlreadyExistsException e) {
            log.warn("user with chatid {} is already registered", chatId);
            return new SendMessage(chatId, "с возвращением! вы уже зарегистрированы. используйте команду /help");

        } catch (ServiceUnavailableException e) {
            log.error("failed to register chatid {}: scrapper service is unavailable", chatId, e);
            return new SendMessage(chatId, "извините, сервис временно недоступен. пожалуйста, попробуйте позже.");

        } catch (Exception e) {
            log.error("unexpected error occurred while registering chatid {} ", chatId, e);
            return new SendMessage(chatId, "произошла непредвиденная ошибка. пожалуйста, попробуйте позже.");
        }
    }
}
