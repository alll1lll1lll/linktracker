package backend.academy.linktracker.scrapper.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SubscriptionId implements Serializable {
    private Long chatId;
    private Long linkId;
}
