package backend.academy.linktracker.scrapper.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.scrapper.client.interfaces.BotClient;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.entity.OutboxEntity;
import backend.academy.linktracker.scrapper.repository.OutboxRepository;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OutboxServiceTest {

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private BotClient transportClient;

    @InjectMocks
    private OutboxService outboxService;

    @Test
    void queueUpdate_shouldSaveEventToRepository() {
        var linkUpdate = new LinkUpdate(1L, URI.create("https://example.com"), "description", List.of(123L));
        ArgumentCaptor<OutboxEntity> entityCaptor = ArgumentCaptor.forClass(OutboxEntity.class);

        outboxService.queueUpdate(linkUpdate);

        verify(outboxRepository, times(1)).save(entityCaptor.capture());
        OutboxEntity capturedEntity = entityCaptor.getValue();

        assertThat(capturedEntity.getLinkId()).isEqualTo(linkUpdate.getId());
        assertThat(capturedEntity.getUrl()).isEqualTo(linkUpdate.getUrl().toString());
        assertThat(capturedEntity.getDescription()).isEqualTo(linkUpdate.getDescription());
        assertThat(capturedEntity.getChatIds()).containsExactlyElementsOf(linkUpdate.getTgChatIds());
    }

    @Test
    void processPendingEvents_shouldSendUpdateAndDeleteEvent_whenSuccessful() {
        OutboxEntity event = OutboxEntity.builder()
                .id(100L)
                .linkId(1L)
                .url("https://example.com")
                .description("description")
                .chatIds(List.of(123L))
                .build();
        when(outboxRepository.findPendingEvents(anyInt())).thenReturn(List.of(event));

        outboxService.processPendingEvents(10);

        verify(transportClient, times(1)).sendUpdate(any(LinkUpdate.class));
        verify(outboxRepository, times(1)).delete(event.getId());
    }

    @Test
    void processPendingEvents_shouldNotDeleteEvent_whenSendUpdateFails() {
        OutboxEntity event = OutboxEntity.builder()
                .id(100L)
                .linkId(1L)
                .url("https://example.com/test")
                .description("A test event")
                .chatIds(List.of(12345L))
                .build();

        when(outboxRepository.findPendingEvents(anyInt())).thenReturn(List.of(event));
        doThrow(new RuntimeException("Kafka is down")).when(transportClient).sendUpdate(any(LinkUpdate.class));

        outboxService.processPendingEvents(10);

        verify(transportClient, times(1)).sendUpdate(any(LinkUpdate.class));
        verify(outboxRepository, never()).delete(anyLong());
    }
}
