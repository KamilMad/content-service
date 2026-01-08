package pl.kamil.content_service.dtos;

import lombok.Builder;

@Builder
public record LessonContentResponse(
        String fileText,
        long totalWords
) {

    public static LessonContentResponse from(String fileText, long totalWords) {
        return new LessonContentResponse(fileText, totalWords);
    }
}
