package pl.kamil.content_service.dtos;

public record LessonRequest(
        String title,
        String content
) { }