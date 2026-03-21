package backend.academy.linktracker.scrapper.dto.request;

import java.net.URI;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AddLinkRequest {
    private URI link;
    private List<String> tags;
}
