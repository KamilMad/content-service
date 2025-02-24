package pl.kamil.content_service.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LessonRequest(
        @NotBlank(message = "Title cannot be empty")
        @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
        String title,

        @NotBlank(message = "Content cannot be empty")
        @Size(min = 10, max = 10000, message = "Content must be between 10 and 10000 characters")
        String content
) { }