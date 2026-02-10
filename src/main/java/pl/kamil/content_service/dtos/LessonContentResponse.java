package pl.kamil.content_service.dtos;

import lombok.Builder;

@Builder
public record LessonContentResponse(
        PagedResponse<String> pagedResponse,
        long totalWords
) {
}
