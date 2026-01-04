package pl.kamil.content_service.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.kamil.content_service.common.ErrorMessages;
import pl.kamil.content_service.dtos.FileUploadResponse;
import pl.kamil.content_service.dtos.LessonResponse;
import pl.kamil.content_service.dtos.LessonsResponse;
import pl.kamil.content_service.events.LessonDeleteEvent;
import pl.kamil.content_service.exceptions.LessonNotFoundException;
import pl.kamil.content_service.models.Content;
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
    private final ContentService contentService;
    private final ApplicationEventPublisher eventPublisher;

    public UUID createLesson(MultipartFile file, UUID userId) {

        FileUploadResponse uploadResponse = uploadFile(file);
        Content content = createContentEntity(file, uploadResponse);
        Lesson lesson = createLessonEntity(file, content, userId);
        return saveLesson(lesson);
    }

    public LessonResponse getById(UUID lessonId, UUID userId) {

        Lesson lesson = lessonRepository.findByIdAndCreatedBy(lessonId, userId)
                .orElseThrow(() -> new LessonNotFoundException(ErrorMessages.LESSON_NOT_FOUND));

        return LessonResponse.from(lesson);
    }

    public LessonsResponse getAll(UUID userId) {
        List<Lesson> lessons = lessonRepository.findAllByCreatedBy(userId);
        List<LessonResponse> lessonResponses = lessons.stream()
                .map(LessonResponse::from)
                .toList();

        return LessonsResponse.from(lessonResponses);
    }

// TODO: Make it transactional? What if fileStorageClient fail?
    @Transactional
    public void deleteLesson(UUID lessonId, UUID userId) {

        Lesson lesson = lessonRepository.findByIdAndCreatedBy(lessonId, userId)
                .orElseThrow(() -> new LessonNotFoundException(ErrorMessages.LESSON_NOT_FOUND));

        String fileKey = lesson.getContent().getS3Key();

        lessonRepository.delete(lesson);

        eventPublisher.publishEvent(
                new LessonDeleteEvent(lesson.getId(), lesson.getContent().getS3Key())
        );
        fileStorageClient.deleteFile(fileKey);
    }

    public String getLessonContent(UUID lessonId, UUID userId) {
        Lesson lesson = lessonRepository.findByIdAndCreatedBy(lessonId, userId)
                .orElseThrow(() -> new LessonNotFoundException(ErrorMessages.LESSON_NOT_FOUND));

        //String fileKey = lesson.getS3Key();

        return fileStorageClient.getFileContent(fileKey);
    }
    private FileUploadResponse uploadFile(MultipartFile file) {
        return fileStorageClient.storeFile(file);
    }

    private Lesson createLessonEntity(MultipartFile file, Content content, UUID userId) {
        Lesson lesson = buildLessonEntity(file.getOriginalFilename(), userId);
        lesson.setContent(content);
        return lesson;
    }

    private Lesson buildLessonEntity(String filename, UUID userId) {
        return Lesson.builder()
                .title(filename)
                .createdBy(userId)
                .build();
    }

    private Content createContentEntity(MultipartFile file, FileUploadResponse uploadResponse) {
        String title = file.getOriginalFilename();
        long totalWords = TextAnalyzer.countWordsInFile(file);

        return contentService.createContent(uploadResponse.s3Key(), totalWords);
    }

    private UUID saveLesson(Lesson lesson) {
        Lesson saved = lessonRepository.save(lesson);
        return saved.getId();
    }
}
