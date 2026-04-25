package backend.academy.linktracker.scrapper.model;

import java.net.URI;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LinkModel {
    private Long id;
    private URI url;
    private OffsetDateTime lastUpdated;
    private OffsetDateTime lastCheckedAt;
}
