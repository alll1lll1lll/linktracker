package backend.academy.linktracker.bot.dto;

import java.net.URI;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LinkResponse {
    private Long id;
    private URI url;
    private List<String> tags;
}
