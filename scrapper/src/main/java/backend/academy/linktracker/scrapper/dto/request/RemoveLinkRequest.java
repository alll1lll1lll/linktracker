package backend.academy.linktracker.scrapper.dto.request;

import java.net.URI;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RemoveLinkRequest {
    private URI link;
}
