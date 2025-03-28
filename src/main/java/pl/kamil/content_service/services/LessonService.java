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
import pl.kamil.content_service.models.Lesson;
import pl.kamil.content_service.repositories.LessonRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;


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
        uploadFile(multipartFile, savedLesson.getId(), savedLesson.getCreatedBy());

        return savedLesson.getId();
    }


    public String uploadFile(MultipartFile file, long lessonId, long userId) throws IOException {

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        body.add("file", new ByteArrayResource(file.getBytes()){
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });


        body.add("lessonId", lessonId);
        body.add("userId", userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:8080/files", request, String.class);

        return response.getBody();
    }

    public void deleteByKey(String key) {
        restTemplate.postForEntity("http://localhost:8081/files/{key}", key, String.class);
    }

    private long calculateTotalWords(String content) {
        String[] words = content.trim().split("\\s+");
        return words.length;
    }
}
