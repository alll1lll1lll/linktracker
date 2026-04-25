package backend.academy.linktracker.scrapper.dto.response.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Setter
public class StackOverflowResponse {
    @JsonProperty("items")
    private List<StackOverflowItem> items;

    @Setter
    @Getter
    @NoArgsConstructor
    public static class StackOverflowItem {
        @JsonProperty("last_activity_date")
        private OffsetDateTime lastActivityDate;

        @JsonProperty("question_id")
        private Long questionId;

        @JsonProperty("title")
        private String title;
    }
}
