package pl.kamil.content_service.dtos;

import lombok.Builder;
import pl.kamil.content_service.models.Lesson;

import java.time.Instant;
import java.util.UUID;

@Builder(toBuilder = true)
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
