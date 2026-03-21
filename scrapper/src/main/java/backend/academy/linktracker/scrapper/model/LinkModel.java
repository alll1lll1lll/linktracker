package backend.academy.linktracker.scrapper.model;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
    private Map<Long, List<String>> chatSubscribers = new ConcurrentHashMap<>();
}
