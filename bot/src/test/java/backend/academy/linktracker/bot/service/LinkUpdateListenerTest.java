package backend.academy.linktracker.bot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.avro.LinkUpdateEvent;
import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.bot.service.notification.LinkUpdateListener;
import backend.academy.linktracker.bot.service.notification.NotificationService;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class LinkUpdateListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private LinkUpdateListener linkUpdateListener;

    @BeforeEach
    void setUp() {
        Set<Long> processedMessageIds = ConcurrentHashMap.newKeySet();
        ReflectionTestUtils.setField(linkUpdateListener, "processedMessageIds", processedMessageIds);
    }

    @Test
    void listen_shouldProcessValidMessage() {
        LinkUpdateEvent event = LinkUpdateEvent.newBuilder()
                .setId(1L)
                .setUrl("https://test.com")
                .setDescription("Update")
                .setTgChatIds(List.of(123L, 456L))
                .build();
        ConsumerRecord<String, LinkUpdateEvent> record = new ConsumerRecord<>("topic", 0, 0, "key", event);
        ArgumentCaptor<LinkUpdate> captor = ArgumentCaptor.forClass(LinkUpdate.class);

        linkUpdateListener.listen(record);

        verify(notificationService, times(1)).processUpdate(captor.capture());
        LinkUpdate capturedUpdate = captor.getValue();
        assertThat(capturedUpdate.getUrl()).isEqualTo(URI.create("https://test.com"));
        assertThat(capturedUpdate.getTgChatIds()).containsExactly(123L, 456L);
        assertThat(capturedUpdate.getDescription()).isEqualTo("Update");
    }

    @Test
    void listen_shouldIgnoreDuplicateMessage() {
        LinkUpdateEvent event = LinkUpdateEvent.newBuilder()
                .setId(2L)
                .setUrl("https://test.com")
                .setDescription("Update")
                .setTgChatIds(List.of(123L))
                .build();
        ConsumerRecord<String, LinkUpdateEvent> record = new ConsumerRecord<>("topic", 0, 0, "key", event);

        linkUpdateListener.listen(record);
        linkUpdateListener.listen(record);

        verify(notificationService, times(1)).processUpdate(any(LinkUpdate.class));
    }

    @Test
    void listen_shouldThrowExceptionForInvalidMessage_andNotProcess() {
        LinkUpdateEvent invalidEvent = LinkUpdateEvent.newBuilder()
                .setId(3L)
                .setUrl("")
                .setDescription("Invalid")
                .setTgChatIds(List.of(123L))
                .build();
        ConsumerRecord<String, LinkUpdateEvent> record = new ConsumerRecord<>("topic", 0, 0, "key", invalidEvent);

        assertThatThrownBy(() -> linkUpdateListener.listen(record))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("URL cannot be null or empty");

        verify(notificationService, never()).processUpdate(any(LinkUpdate.class));
    }

    @Test
    void listen_shouldThrowExceptionIfProcessingFails_forRetryMechanism() {
        LinkUpdateEvent event = LinkUpdateEvent.newBuilder()
                .setId(4L)
                .setUrl("https://test.com")
                .setDescription("Update")
                .setTgChatIds(List.of(123L))
                .build();
        ConsumerRecord<String, LinkUpdateEvent> record = new ConsumerRecord<>("topic", 0, 0, "key", event);

        doThrow(new RuntimeException("Telegram API is down"))
                .when(notificationService)
                .processUpdate(any());

        assertThatThrownBy(() -> linkUpdateListener.listen(record))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Telegram API is down");

        verify(notificationService, times(1)).processUpdate(any(LinkUpdate.class));
    }
}
