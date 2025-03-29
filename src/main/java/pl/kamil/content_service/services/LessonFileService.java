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
import pl.kamil.content_service.repositories.LessonRepository;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class LessonFileService {

    private final RestTemplate restTemplate;

    public FileUploadResponse uploadFile(MultipartFile file, long lessonId, long userId) throws IOException {

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

        ResponseEntity<FileUploadResponse> response = restTemplate.postForEntity("http://localhost:8080/files", request, FileUploadResponse.class);

        return response.getBody();
    }
}
