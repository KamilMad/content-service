package pl.kamil.content_service.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> createLesson(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") Long userId) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be null or empty");
        }

        Long id = lessonService.createLesson(file, userId);

        return ResponseEntity.ok().body(id);
    }

    @GetMapping
    public ResponseEntity<LessonsResponse> getLessons(
            @RequestHeader("X-User-Id") Long userId) {

        LessonsResponse response = lessonService.getAll(userId);

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{lessonId}")
    public ResponseEntity<LessonResponse> getLessonById(
            @PathVariable Long lessonId,
            @RequestHeader("X-User-Id") Long userId) {
        LessonResponse response = lessonService.getById(lessonId, userId);

        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{lessonId}")
    public ResponseEntity<Void> deleteLesson(
            @PathVariable Long lessonId,
            @RequestHeader("X-User-Id") Long userId) {

        lessonService.deleteById(lessonId, userId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<String> getContent(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {

        String content = lessonService.getLessonContent(id, userId);

        return ResponseEntity.ok().body(content);
    }

}
