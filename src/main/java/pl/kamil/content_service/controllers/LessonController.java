package pl.kamil.content_service.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.kamil.content_service.dtos.FileUploadRequest;
import pl.kamil.content_service.dtos.LessonContentResponse;
import pl.kamil.content_service.dtos.LessonResponse;
import pl.kamil.content_service.dtos.PagedResponse;
import pl.kamil.content_service.services.LessonService;
import pl.kamil.content_service.validation.annotations.CurrentUserId;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/content")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LessonResponse> createLesson(
            @CurrentUserId UUID userId,
            @Valid @ModelAttribute  FileUploadRequest request) throws IOException {

        log.info("Controller : User id is {}", userId);
         LessonResponse response =  lessonService.createLesson(request.file(), userId);

         URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<LessonResponse>> getLessons(
            @CurrentUserId  UUID userId,
            @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

         PagedResponse<LessonResponse> response = lessonService.getAllLessons(userId, pageNo, pageSize);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LessonResponse> getLessonById(
            @PathVariable("id")  UUID lessonId,
            @CurrentUserId UUID userId) {

         LessonResponse response = lessonService.getLesson(lessonId, userId);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLesson(
            @PathVariable("id")  UUID lessonId,
            @CurrentUserId UUID userId) {

        lessonService.deleteLesson(lessonId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<LessonContentResponse> getContent(
            @PathVariable("id")  UUID lessonId,
            @CurrentUserId UUID userId) {

         LessonContentResponse contentResponse = lessonService.getLessonContent(lessonId, userId);
        return ResponseEntity.ok().body(contentResponse);
    }

}
