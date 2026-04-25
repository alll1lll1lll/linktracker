package backend.academy.linktracker.scrapper.dto.response.stackoverflow;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StackOverflowAnswerResponse {
    private List<StackOverflowAnswerItemResponse> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StackOverflowAnswerItemResponse {
        private StackOverflowOwnerResponse owner;

        private long creationDate;

        private String body;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StackOverflowOwnerResponse {
        private String displayName;
    }
}
