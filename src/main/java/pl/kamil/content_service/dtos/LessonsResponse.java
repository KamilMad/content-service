package pl.kamil.content_service.dtos;

import org.springframework.data.domain.Page;

import java.util.List;

public record LessonsResponse(
        List<LessonResponse> lessons,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {

    public static LessonsResponse from(Page<LessonResponse> page) {
        return new LessonsResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
