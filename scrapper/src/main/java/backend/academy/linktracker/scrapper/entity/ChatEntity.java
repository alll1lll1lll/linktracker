package backend.academy.linktracker.scrapper.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "chat")
@Getter
@Setter
public class ChatEntity {
    @Id
    private Long id;
}
