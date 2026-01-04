package pl.kamil.content_service.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.kamil.content_service.common.ErrorMessages;
import pl.kamil.content_service.dtos.FileUploadResponse;
import pl.kamil.content_service.dtos.LessonResponse;
import pl.kamil.content_service.dtos.LessonsResponse;
import pl.kamil.content_service.exceptions.AccessDeniedException;
import pl.kamil.content_service.exceptions.LessonNotFoundException;
import pl.kamil.content_service.models.Lesson;
import pl.kamil.content_service.repositories.LessonRepository;
import pl.kamil.content_service.util.TextAnalyzer;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final FileStorage fileStorageClient;

    public UUID createLesson(MultipartFile file, UUID userId) {
        FileUploadResponse response = fileStorageClient.storeFile(file);

        Lesson lesson = createLessonFromFile(file, userId);
        updateLessonWithS3Key(lesson, response.s3Key());
        Lesson savedLesson = lessonRepository.save(lesson);

        return savedLesson.getId();
    }

    public LessonResponse getById(UUID lessonId, UUID userId) {

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException(ErrorMessages.LESSON_NOT_FOUND));

        validateOwnership(lesson, userId);

        return LessonResponse.from(lesson);
    }

    public LessonsResponse getAll(UUID userId) {
        List<Lesson> lessons = lessonRepository.findAllByCreatedBy(userId);
        List<LessonResponse> lessonResponses = lessons.stream()
                .map(LessonResponse::from)
                .toList();

        return LessonsResponse.from(lessonResponses);
    }

    public void deleteById(UUID lessonId, UUID userId) {

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException(ErrorMessages.LESSON_NOT_FOUND));

        validateOwnership(lesson, userId);

        String fileKey = lesson.getS3Key();
        fileStorageClient.deleteFile(fileKey);
        lessonRepository.deleteById(lessonId);
    }

    public String getLessonContent(UUID lessonId, UUID userId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException(ErrorMessages.LESSON_NOT_FOUND));

        validateOwnership(lesson, userId);

        String fileKey = lesson.getS3Key();

        return fileStorageClient.getFileContent(fileKey);
    }

    private void updateLessonWithS3Key(Lesson lesson, String key) {
        lesson.setS3Key(key);
    }

    private Lesson createLessonFromFile(MultipartFile file, UUID userId) {
        long totalWords = TextAnalyzer.countWordsInFile(file);
        return Lesson.create(file.getOriginalFilename(), userId, totalWords);
    }

    private void validateOwnership(Lesson lesson, UUID userId) {
        if (!lesson.getCreatedBy().equals(userId)) {
            throw new AccessDeniedException(ErrorMessages.ACCESS_DENIED);
        }
    }

}
