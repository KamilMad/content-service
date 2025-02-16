package pl.kamil.content_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pl.kamil.content_service.dtos.LessonRequest;

@RestController
@RequestMapping("/lessons")
public class LessonController {

    //upload lesson
    @PostMapping
    public ResponseEntity<String> uploadFile(
            @RequestPart LessonRequest lessonRequest,
            @RequestPart MultipartFile audioFile) {

        return ResponseEntity.ok("Successfully uploaded a file");
    }

    //get lesson
}
