package pl.kamil.content_service.application;

import java.util.UUID;

public record LessonDeleteEvent(UUID lessonId, String s3Key) {
}
