package pl.kamil.content_service.events;

import java.util.UUID;

public record LessonDeleteEvent(UUID lessonId, String s3Key) {
}
