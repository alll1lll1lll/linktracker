package backend.academy.linktracker.bot.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class LinkUpdate {
    @NotNull
    private Long id;

    @NotNull
    private URI url;

    private String description;

    @NotEmpty
    private List<Long> tgChatIds;
}
