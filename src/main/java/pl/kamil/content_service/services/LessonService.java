package pl.kamil.content_service.services;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import pl.kamil.content_service.dtos.FileUploadResponse;
import pl.kamil.content_service.models.Lesson;
import pl.kamil.content_service.repositories.LessonRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final RestTemplate restTemplate;

    public Long createLesson(MultipartFile multipartFile, String lessonTitle) throws IOException {
        // Save Lesson metadata to db
        Lesson lesson = new Lesson();
        lesson.setTitle(lessonTitle);
        lesson.setTotal_words(calculateTotalWords(new String(multipartFile.getBytes(), StandardCharsets.UTF_8)));
        lesson.setCreated_at(Instant.now());
        lesson.setUpdated_at(Instant.now());
        lesson.setCreatedBy(1L);

        Lesson savedLesson = lessonRepository.save(lesson);

        //call FileUploadServiceApi to upload file to S3
        FileUploadResponse response = uploadFile(multipartFile, savedLesson.getId(), savedLesson.getCreatedBy());

        savedLesson.setFileUrl(response.url());
        savedLesson = lessonRepository.save(savedLesson);

        return savedLesson.getId();
    }

    private void attachFileToLesson(Long lessonId, String fileUrl) {
        Optional<Lesson> lessonOptional = lessonRepository.findById(lessonId);

        if (lessonOptional.isEmpty()) {
            throw new RuntimeException("Lesson doesn't exist");
        }

        Lesson lesson = lessonOptional.get();
        lesson.setFileUrl(fileUrl);

        lessonRepository.save(lesson);
    }

    public void deleteByKey(String key) {
        restTemplate.postForEntity("http://localhost:8081/files/{key}", key, String.class);
    }

    private long calculateTotalWords(String content) {
        String[] words = content.trim().split("\\s+");
        return words.length;
    }
}
