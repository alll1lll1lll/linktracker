package backend.academy.linktracker.scrapper.entity;

import backend.academy.linktracker.scrapper.model.SubscriptionId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "subscription")
@IdClass(SubscriptionId.class)
@Getter
@Setter
public class SubscriptionEntity {
    @Id
    @Column(name = "chat_id")
    private Long chatId;

    @Id
    @Column(name = "link_id")
    private Long linkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "link_id", insertable = false, updatable = false)
    private LinkEntity link;

    @ManyToMany
    @JoinTable(
            name = "subscription_tag",
            joinColumns = {
                @JoinColumn(name = "chat_id", referencedColumnName = "chat_id"),
                @JoinColumn(name = "link_id", referencedColumnName = "link_id")
            },
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<TagEntity> tags = new ArrayList<>();
}
