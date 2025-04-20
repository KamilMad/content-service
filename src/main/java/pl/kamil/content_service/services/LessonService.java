package pl.kamil.content_service.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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

    public Long createLesson(MultipartFile multipartFile, String lessonTitle, Long userId) {

        Lesson lesson = createLessonFromFile(lessonTitle, multipartFile, userId);
        // Save Lesson metadata to db
        Lesson savedLesson = lessonRepository.save(lesson);
        //call FileUploadServiceApi to upload file to S3
        FileUploadResponse response = lessonFileService.uploadFile(multipartFile, savedLesson.getId(), savedLesson.getCreatedBy());
        updateLessonWithFileUrl(savedLesson, response.url());

        return savedLesson.getId();
    }

    public LessonResponse getById(Long lessonId, Long userId) {

        Lesson lesson = lessonRepository.findById(lessonId)
               .orElseThrow(() -> new LessonNotFoundException("Lesson not found"));

        validateOwnership(lesson, userId);

        return LessonResponse.from(lesson);
    }

    public LessonsResponse getAll(Long userId) {
        List<Lesson> lessons = lessonRepository.findAllByCreatedBy(userId);
        List<LessonResponse> lessonResponses = lessons.stream()
                .map(LessonResponse::from)
                .toList();

        return LessonsResponse.from(lessonResponses);
    }

    public void deleteById(Long lessonId, Long userId) {

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException("Lesson not found"));

        validateOwnership(lesson, userId);

        String fileKey = extractKeyFromUrl(lesson.getFileUrl());
        lessonFileService.deleteFile(fileKey);
        lessonRepository.deleteById(lessonId);
    }

    public String getLessonContent(Long lessonId, Long userId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException("Lesson not found"));

        validateOwnership(lesson, userId);

        String fileKey = extractKeyFromUrl(lesson.getFileUrl());

        return lessonFileService.getFileContent(fileKey);
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

    private String extractKeyFromUrl(String url) {
        URI uri = URI.create(url);
        String path = uri.getPath(); // e.g. /lessons/abc.txt

        return path.substring(path.lastIndexOf('/') + 1); // → abc.txt
    }


    private void validateOwnership(Lesson lesson, Long userId) {
        if (!lesson.getCreatedBy().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to access this lesson");
        }
    }
}
