package pl.kamil.content_service.api.response;

import lombok.Builder;

@Builder
public record LessonContentResponse(
        PagedResponse<String> pagedResponse,
        long totalWords
) {
}
