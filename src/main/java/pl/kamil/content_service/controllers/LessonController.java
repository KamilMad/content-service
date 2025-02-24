package pl.kamil.content_service.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamil.content_service.dtos.LessonRequest;
import pl.kamil.content_service.models.Lesson;
import pl.kamil.content_service.services.LessonService;


@RestController
@RequestMapping("/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    //upload lesson
    @PostMapping("/uploadFile")
    public ResponseEntity<Lesson> uploadFile(
            @RequestPart("title") String title,
            @RequestPart("content") String content) {

        LessonRequest lessonRequest = new LessonRequest(title, content);
        Lesson createdLesson = lessonService.createLesson(lessonRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLesson);
    }

    //get lesson
}
