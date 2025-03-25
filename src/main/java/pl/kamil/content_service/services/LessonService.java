package pl.kamil.content_service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
import pl.kamil.content_service.dtos.LessonRequest;
import pl.kamil.content_service.exceptions.LessonNotFoundException;
import pl.kamil.content_service.models.Lesson;
import pl.kamil.content_service.repositories.LessonRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    //private final RabbitTemplate rabbitTemplate;
    private final RestTemplate restTemplate;

//    public List<Lesson> findAll() {
//        return lessonRepository.findAll();
//    }
//
//    public void saveLessonFromFile(MultipartFile file) throws IOException {
//
//        // Call the /file/upload endpoint from FileUploadService
//
//        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
//        Lesson lesson = new Lesson();
//        lesson.setTitle(file.getOriginalFilename());
//
//        lesson.setTotal_words(calculateTotalWords(file));
//
//        lesson.setCreated_at(Instant.now());
//        lesson.setUpdated_at(Instant.now());
//        //lesson.setContent(content);
//
//        lessonRepository.save(lesson);
//    }
//
//    public Lesson findById(Long id) {
//        Optional<Lesson> optionalLesson = lessonRepository.findById(id);
//
//        if (optionalLesson.isEmpty()) {
//            throw new LessonNotFoundException("Lesson with id: " + id + " not found");
//        }
//
//        return optionalLesson.get();
//    }
//
//    public void deleteById(Long id) {
//        if (!lessonRepository.existsById(id)) {
//            throw new LessonNotFoundException("Lesson with id: " + id + " not found");
//        }
//
//        lessonRepository.deleteById(id);
//    }
//
//    private long calculateTotalWords(MultipartFile file) throws IOException {
//        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
//
//        return content.split("\\s+").length;
//
//    }
//
//    public void requestFileUpload(LessonRequest request) {
//        ObjectMapper objectMapper = new ObjectMapper();
//        try {
//            // Convert LessonRequest object to JSON string
//            String jsonMessage = objectMapper.writeValueAsString(request);
//            rabbitTemplate.convertAndSend("uploadQueue", jsonMessage);
//            System.out.println("📤 Sent message as JSON: " + jsonMessage);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public String uploadFile(MultipartFile file) throws IOException {

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        body.add("file", new ByteArrayResource(file.getBytes()){
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:8080/files", request, String.class);

        return response.getBody();
    }

    public void deleteByKey(String key) {
        restTemplate.postForEntity("http://localhost:8081/files/{key}", key, String.class);
    }
}
