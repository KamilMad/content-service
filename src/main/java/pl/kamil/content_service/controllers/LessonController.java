package pl.kamil.content_service.controllers;

import jakarta.validation.Valid;
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
    public ResponseEntity<Lesson> uploadFile(@RequestBody @Valid LessonRequest lessonRequest) {

        Lesson createdLesson = lessonService.createLesson(lessonRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLesson);
    }

    //get lesson
}
