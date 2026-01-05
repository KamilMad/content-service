package pl.kamil.content_service.dtos;

public record LessonContentResponse(
        String fileText,
        long totalWords
) {

    public static LessonContentResponse from(String fileText, long totalWords) {
        return new LessonContentResponse(fileText, totalWords);
    }
}
