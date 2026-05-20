package pl.kamil.content_service.application.event;

import java.util.UUID;

public record LessonDeleteEvent(UUID lessonId, String s3Key) {
}
