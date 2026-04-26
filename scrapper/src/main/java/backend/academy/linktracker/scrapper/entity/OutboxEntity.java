package backend.academy.linktracker.scrapper.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "outbox_event")
@Getter
@Setter
@Builder
public class OutboxEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "link_id", nullable = false)
    private Long linkId;

    @Column(nullable = false)
    private String url;

    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "outbox_event_chat_ids", joinColumns = @JoinColumn(name = "outbox_event_id"))
    @Column(name = "chat_id")
    private List<Long> chatIds;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
