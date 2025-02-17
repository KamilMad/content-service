package pl.kamil.content_service.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.kamil.content_service.models.Lesson;
import pl.kamil.content_service.services.LessonService;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    //upload lesson
    @PostMapping("/uploadFile")
    public ResponseEntity<String> uploadFile(
            @RequestPart("file")MultipartFile file) {

        String content = "";

        try {
            content = new String(file.getBytes(), StandardCharsets.UTF_8);
        }catch (Exception e) {
            System.out.println("Lypaaaa");
        }


        Lesson lesson = Lesson.builder()
                .title(file.getOriginalFilename())
                .content(content)
                .build();

        lessonService.save(lesson);
        return ResponseEntity.ok("Successfully uploaded a file");
    }

    //get lesson
}
