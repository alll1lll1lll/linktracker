package backend.academy.linktracker.scrapper.dto.response;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDetails {
    private String title;
    private String author;
    private OffsetDateTime createdAt;
    private String preview;
    private String updateType;
}
