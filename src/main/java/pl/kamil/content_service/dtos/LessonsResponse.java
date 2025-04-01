package pl.kamil.content_service.dtos;

import java.util.List;

public record LessonsResponse(
        List<LessonResponse> lessons,
        int total
) {

    public static LessonsResponse from(List<LessonResponse> list) {
        return new LessonsResponse(list, list.size());
    }
}
