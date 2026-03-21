package backend.academy.linktracker.scrapper.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StackOverflowResponse {
    @JsonProperty("items")
    private List<StackOverflowItem> items;
}
