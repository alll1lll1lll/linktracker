package backend.academy.linktracker.scrapper.dto.response.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StackOverflowCommentResponse {

    @JsonProperty("items")
    private List<StackOverflowCommentItemResponse> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StackOverflowCommentItemResponse {

        private StackOverflowOwnerResponse owner;

        @JsonProperty("creation_date")
        private long creationDate;

        private String body;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StackOverflowOwnerResponse {
        @JsonProperty("display_name")
        private String displayName;
    }
}
