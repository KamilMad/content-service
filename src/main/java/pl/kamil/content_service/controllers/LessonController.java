package pl.kamil.content_service.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.kamil.content_service.dtos.LessonRequest;
import pl.kamil.content_service.models.Lesson;
import pl.kamil.content_service.services.LessonService;

import java.util.List;

@CrossOrigin(origins = "http://127.0.0.1:5500")  // Allow frontend access
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

    @PostMapping("upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            lessonService.saveLessonFromFile(file);
            return ResponseEntity.ok("File uploaded successfully");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload failed: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Lesson>> getAllLessons() {
        List<Lesson> lessons = lessonService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(lessons);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lesson> getLessonById(@PathVariable Long id) {
        Lesson lesson = lessonService.findById(id);

        return ResponseEntity.status(HttpStatus.OK).body(lesson);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable Long id) {
        lessonService.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body("Lesson with id: " + id + " was removed");
    }
}
