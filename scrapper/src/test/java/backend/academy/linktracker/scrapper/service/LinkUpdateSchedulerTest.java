package backend.academy.linktracker.scrapper.service;

import static org.mockito.Mockito.*;

import backend.academy.linktracker.scrapper.model.LinkModel;
import backend.academy.linktracker.scrapper.properties.SchedulerProperties;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.scheduler.LinkUpdaterScheduler;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LinkUpdateSchedulerTest {

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private LinkUpdateService linkUpdateService;

    @Mock
    private SchedulerProperties properties;

    @Mock
    private LinkErrorReportService linkErrorReportService;

    private LinkUpdaterScheduler scheduler;

    @BeforeEach
    void setUp() {
        lenient().when(properties.threads()).thenReturn(4);
        lenient().when(properties.batchSize()).thenReturn(10);
        lenient().when(properties.offset()).thenReturn(0);

        scheduler = new LinkUpdaterScheduler(linkRepository, linkUpdateService, linkErrorReportService, properties);
    }

    @Test
    void shouldProcessSecondLinkIfFirstFails() {
        LinkModel link1 = new LinkModel(1L, URI.create("url1"), OffsetDateTime.now(), OffsetDateTime.now());
        LinkModel link2 = new LinkModel(2L, URI.create("url2"), OffsetDateTime.now(), OffsetDateTime.now());

        when(linkRepository.findLinksToUpdate(eq(10), any(OffsetDateTime.class)))
                .thenReturn(List.of(link1, link2));

        when(linkUpdateService.processUpdate(link1)).thenThrow(new RuntimeException("API Error"));
        when(linkUpdateService.processUpdate(link2)).thenReturn(true);

        scheduler.update();

        verify(linkUpdateService).processUpdate(link1);
        verify(linkUpdateService).processUpdate(link2);
    }
}
