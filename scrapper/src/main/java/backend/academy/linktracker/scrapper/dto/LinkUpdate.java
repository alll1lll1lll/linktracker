package backend.academy.linktracker.scrapper.dto;

import java.net.URI;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LinkUpdate {
    private Long id;
    private URI url;
    private String description;
    private List<Long> tgChatIds;
}
