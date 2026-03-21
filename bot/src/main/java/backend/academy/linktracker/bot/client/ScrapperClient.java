package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.bot.dto.*;
import backend.academy.linktracker.bot.exception.*;

public interface ScrapperClient {

    void register(long chatId) throws ChatAlreadyExistsException, ServiceUnavailableException;

    void delete(long chatId) throws ServiceUnavailableException;

    void addLink(long chatId, AddLinkRequest request) throws LinkAlreadyExistsException, ServiceUnavailableException;

    void removeLink(long chatId, RemoveLinkRequest request) throws LinkNotFoundException, ServiceUnavailableException;

    ListLinksResponse getLinks(long chatId) throws ServiceUnavailableException;
}
