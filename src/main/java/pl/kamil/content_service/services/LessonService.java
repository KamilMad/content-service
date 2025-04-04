package pl.kamil.content_service.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.kamil.content_service.dtos.FileUploadResponse;
import pl.kamil.content_service.dtos.LessonResponse;
import pl.kamil.content_service.dtos.LessonsResponse;
import pl.kamil.content_service.exceptions.LessonNotFoundException;
import pl.kamil.content_service.models.Lesson;
import pl.kamil.content_service.repositories.LessonRepository;

import java.io.IOException;
import java.time.Instant;
import java.util.List;


@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final LessonFileService lessonFileService;
    private final TextAnalysisService textAnalysisService;

    public Long createLesson(MultipartFile multipartFile, String lessonTitle) throws IOException {

        Lesson lesson = createLessonFromFile(lessonTitle, multipartFile, 1L);
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
                .orElseThrow(() -> new LessonNotFoundException("Lesson with id: " + id + " not funnd"));

        return LessonResponse.from(lesson);
    }

    public LessonsResponse getAll() {
        List<Lesson> lessons = lessonRepository.findAll();
        List<LessonResponse> lessonResponses = lessons.stream()
                .map(LessonResponse::from)
                .toList();

        return LessonsResponse.from(lessonResponses);
    }

    public void deleteById(String key) {
        lessonFileService.deleteFile(key);
    }
}
