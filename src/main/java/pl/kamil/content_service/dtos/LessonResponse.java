package pl.kamil.content_service.dtos;

import pl.kamil.content_service.models.Lesson;

import java.time.Instant;

public record LessonResponse(
        Long id,
        String title,
        long totalWords,
        Instant created_at,
        String s3Key
) {

    public static LessonResponse from(Lesson lesson) {
        return new LessonResponse(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getTotal_words(),
                lesson.getCreated_at(),
                lesson.getS3Key()
        );
    }
}
