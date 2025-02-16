package pl.kamil.content_service.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import pl.kamil.content_service.dtos.LessonRequest;
import pl.kamil.content_service.models.Lesson;
import pl.kamil.content_service.services.LessonService;

@RestController
@RequestMapping("/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    //upload lesson
    @PostMapping
    public ResponseEntity<String> uploadFile(
            @RequestPart LessonRequest lessonRequest) {

        Lesson lesson = Lesson.builder()
                .title(lessonRequest.title())
                .content(lessonRequest.content())
                .build();

        lessonService.save(lesson);
        return ResponseEntity.ok("Successfully uploaded a file");
    }

    //get lesson
}
