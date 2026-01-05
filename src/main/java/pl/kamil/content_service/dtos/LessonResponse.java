package pl.kamil.content_service.dtos;

import pl.kamil.content_service.models.Lesson;

import java.time.Instant;
import java.util.UUID;

public record LessonResponse(
        UUID id,
        String title,
        Instant createdAt,
        Instant updatedAt
) {

    public static LessonResponse from(Lesson lesson) {
        return new LessonResponse(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getCreatedAt(),
                lesson.getUpdatedAt()
        );
    }
}
