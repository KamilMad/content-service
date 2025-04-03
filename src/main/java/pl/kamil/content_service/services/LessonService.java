package pl.kamil.content_service.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.kamil.content_service.dtos.FileUploadResponse;
import pl.kamil.content_service.dtos.LessonResponse;
import pl.kamil.content_service.dtos.LessonsResponse;
import pl.kamil.content_service.exceptions.LessonNotFoundException;
import pl.kamil.content_service.models.Lesson;
import pl.kamil.content_service.repositories.LessonRepository;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.List;


@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final LessonFileService lessonFileService;
    private final TextAnalysisService textAnalysisService;

    public Long createLesson(MultipartFile multipartFile, String lessonTitle) throws IOException {

        Long userId = getCurrentUserId();
        Lesson lesson = createLessonFromFile(lessonTitle, multipartFile, userId);
        // Save Lesson metadata to db
        Lesson savedLesson = lessonRepository.save(lesson);
        //call FileUploadServiceApi to upload file to S3
        FileUploadResponse response = lessonFileService.uploadFile(multipartFile, savedLesson.getId(), savedLesson.getCreatedBy());
        updateLessonWithFileUrl(savedLesson, response.url());

        return savedLesson.getId();
    }

    private void updateLessonWithFileUrl(Lesson lesson, String url) {
        lesson.setFileUrl(url);
        lesson.setUpdated_at(Instant.now());
        lessonRepository.save(lesson);
    }

    private Lesson createLessonFromFile(String title, MultipartFile file, Long userId) {
        long totalWords = textAnalysisService.countWordsInFile(file);
        return Lesson.create(title,userId, totalWords);
    }

    public LessonResponse getById(Long id) {

        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new LessonNotFoundException("Lesson with id: " + id + " not found"));

        if (!lesson.getCreatedBy().equals(getCurrentUserId())) {
            throw new RuntimeException("You don't have access to this lesson.");
        }

        return LessonResponse.from(lesson);
    }

//    public LessonsResponse getAll() {
//        List<Lesson> lessons = lessonRepository.findAll();
//        List<LessonResponse> lessonResponses = lessons.stream()
//                .map(LessonResponse::from)
//                .toList();
//
//        return LessonsResponse.from(lessonResponses);
//    }

    public LessonsResponse getAll() {
        Long userId = getCurrentUserId();
        List<Lesson> lessons = lessonRepository.findAllByCreatedBy(userId);
        List<LessonResponse> lessonResponses = lessons.stream()
                .map(LessonResponse::from)
                .toList();

        return LessonsResponse.from(lessonResponses);
    }
    public void deleteById(String key) {
        lessonFileService.deleteFile(key);
    }


    public String getLessonContent(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new LessonNotFoundException("Lesson with id: " + id + " not found"));

        if (!lesson.getCreatedBy().equals(getCurrentUserId())) {
            throw new RuntimeException("You don't have access to this lesson.");
        }

        String fileKey = extractKeyFromUrl(lesson.getFileUrl());

        return lessonFileService.getFileContent(fileKey);
    }


    private String extractKeyFromUrl(String url) {
        URI uri = URI.create(url);
        String path = uri.getPath(); // e.g. /lessons/abc.txt

        return path.substring(path.lastIndexOf('/') + 1); // → abc.txt
    }

    private Long getCurrentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }


}
