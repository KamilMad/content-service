package pl.kamil.content_service.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.kamil.content_service.common.ErrorMessages;
import pl.kamil.content_service.dtos.FileUploadResponse;
import pl.kamil.content_service.dtos.LessonContentResponse;
import pl.kamil.content_service.dtos.LessonResponse;
import pl.kamil.content_service.dtos.PagedResponse;
import pl.kamil.content_service.events.LessonDeleteEvent;
import pl.kamil.content_service.exceptions.ForbiddenAccessException;
import pl.kamil.content_service.exceptions.ResourceNotFoundException;
import pl.kamil.content_service.models.Content;
import pl.kamil.content_service.models.Lesson;
import pl.kamil.content_service.repositories.LessonRepository;
import pl.kamil.content_service.util.TextAnalyzer;

import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final FileStorage fileStorageClient;
    private final ContentService contentService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public LessonResponse createLesson(MultipartFile file, UUID userId) {
        FileUploadResponse uploadResponse = null;
        try {
            uploadResponse = uploadFile(file);
            Content content = createContentEntity(file, uploadResponse);
            Lesson lesson = createLessonEntity(file, content, userId);
            return saveLesson(lesson);
        } catch (Exception e) {
            if (uploadResponse != null) {
                cleanupS3(uploadResponse.s3Key());
            }
            throw e;
        }
    }

    public LessonResponse getLesson(UUID lessonId, UUID userId) {
        Lesson lesson = getLessonOrThrow(lessonId);
        ensureOwnership(lesson, userId);
        return LessonResponse.from(lesson);
    }

    public PagedResponse<LessonResponse> getAllLessons(UUID userId, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Lesson> lessons = lessonRepository.findAllByCreatedBy(userId, pageable);

        Page<LessonResponse> lessonResponses = lessons.map(LessonResponse::from);
        return PagedResponse.from(lessonResponses);
    }

    @Transactional
    public void deleteLesson(UUID lessonId, UUID userId) {
        Lesson lesson = getLessonOrThrow(lessonId);
        ensureOwnership(lesson, userId);
        String fileKey = lesson.getContent().getS3Key();
        lessonRepository.delete(lesson);

        eventPublisher.publishEvent(
                new LessonDeleteEvent(lesson.getId(), fileKey)
        );
    }

    public LessonContentResponse getLessonContent(UUID lessonId, UUID userId, int pageNo, int pageSize) {
        Lesson lesson = getLessonOrThrow(lessonId);
        ensureOwnership(lesson, userId);
        Content content = fetchContent(lesson);
        String fileText =  fetchLessonTextFromS3(content);
        PagedResponse<String> pagedResponse = contentService.createContentPage(fileText, pageNo, pageSize);
        return new LessonContentResponse(pagedResponse, content.getTotalWords());
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
        long totalWords = TextAnalyzer.countWordsInFile(file);
        return contentService.createContent(uploadResponse.s3Key(), totalWords);
    }

    private LessonResponse saveLesson(Lesson lesson) {
        Lesson saved = lessonRepository.save(lesson);
        return LessonResponse.from(saved);
    }

    private void cleanupS3(String s3Key) {
        try {
            fileStorageClient.deleteFile(s3Key);
        } catch (Exception e) {
            log.warn("Failed to cleanup S3 for key {}: {}", s3Key, e.getMessage() );
        }
    }

    private Content fetchContent(Lesson lesson) {
        Content content = lesson.getContent();
        if (content == null) {
            log.warn("Content not found for lesson with id: {}",lesson.getId());
            throw new ResourceNotFoundException(ErrorMessages.LESSON_CONTENT_NOT_FOUND);
        }
        return content;
    }

    private String fetchLessonTextFromS3(Content content) {
        return fileStorageClient.getFileContent(content.getS3Key());
    }

    private Lesson getLessonOrThrow(UUID lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.LESSON_NOT_FOUND));
    }

    private void ensureOwnership(Lesson lesson, UUID userId) {
        if (!lesson.getCreatedBy().equals(userId)) {
            throw new ForbiddenAccessException(ErrorMessages.ACCESS_DENIED);
        }
    }

}
