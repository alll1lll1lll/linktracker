package backend.academy.linktracker.bot.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommandType {
    START("/start", "начало работы бота"),
    HELP("/help", "вывести список всех зарегистрированных команд"),
    TRACK("/track", "начать отслеживание ссылки"),
    UNTRACK("/untrack", "прекратить отслеживание ссылки"),
    LIST("/list", "показать список отслеживаемых ссылок"),
    CANCEL("/cancel", "прервать процесс");

    private final String commandName;
    private final String description;
}
