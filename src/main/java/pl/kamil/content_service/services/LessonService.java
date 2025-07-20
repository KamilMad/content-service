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
import java.time.Instant;
import java.util.List;


@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final LessonFileService lessonFileService;


    public Long createLesson(MultipartFile file, long userId) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be null or empty");
        }

        //call FileUploadServiceApi to upload file to S3
        FileUploadResponse response = lessonFileService.uploadFile(file);

        Lesson lesson = createLessonFromFile(file, userId);
        updateLessonWithS3Key(lesson, response.s3Key());
        Lesson savedLesson = lessonRepository.save(lesson);

        return savedLesson.getId();
    }

    public LessonResponse getById(Long lessonId, Long userId) {

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException(ErrorMessages.LESSON_NOT_FOUND));

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
                .orElseThrow(() -> new LessonNotFoundException(ErrorMessages.LESSON_NOT_FOUND));

        validateOwnership(lesson, userId);

        String fileKey = lesson.getS3Key();
        lessonFileService.deleteFile(fileKey);
        lessonRepository.deleteById(lessonId);
    }

    public String getLessonContent(Long lessonId, Long userId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException(ErrorMessages.LESSON_NOT_FOUND));

        validateOwnership(lesson, userId);

        String fileKey = lesson.getS3Key();

        return lessonFileService.getFileContent(fileKey);
    }

    private void updateLessonWithS3Key(Lesson lesson, String key) {
        lesson.setS3Key(key);
        lesson.setUpdated_at(Instant.now());
    }

    private Lesson createLessonFromFile(MultipartFile file, Long userId) {
        long totalWords = TextAnalyzer.countWordsInFile(file);
        return Lesson.create(file.getOriginalFilename(), userId, totalWords);
    }

    private void validateOwnership(Lesson lesson, Long userId) {
        if (!lesson.getCreatedBy().equals(userId)) {
            throw new AccessDeniedException(ErrorMessages.ACCESS_DENIED);
        }
    }

}
