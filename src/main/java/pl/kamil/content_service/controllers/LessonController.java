package pl.kamil.content_service.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.kamil.content_service.dtos.LessonResponse;
import pl.kamil.content_service.dtos.LessonsResponse;
import pl.kamil.content_service.services.LessonService;

import java.io.IOException;

@CrossOrigin(origins = "http://127.0.0.1:5500")  // Allow frontend access
@RestController
@RequestMapping("/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @PostMapping()
    public ResponseEntity<Long> createLesson(
            @RequestParam MultipartFile file,
            @RequestParam String title) throws IOException {

        Long id = lessonService.createLesson(file, title);

        return ResponseEntity.ok().body(id);
    }

    @GetMapping
    public ResponseEntity<LessonsResponse> getLessons() {
        LessonsResponse response = lessonService.getAll();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LessonResponse> getLessonById(@PathVariable Long id) {
        LessonResponse response = lessonService.getById(id);

        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable String id) {
        lessonService.deleteById(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

//    @DeleteMapping("/delete/{key}")
//    public void deleteFileByKey(@PathVariable String key) {
//
//        lessonService.deleteByKey(key);
//    }
}
