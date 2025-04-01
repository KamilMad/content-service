package pl.kamil.content_service.services;

import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import pl.kamil.content_service.dtos.FileUploadResponse;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class LessonFileService {

    private final RestTemplate restTemplate;
    private static final String FILE_UPLOAD_URL = "http://localhost:8080/files";

    public FileUploadResponse uploadFile(MultipartFile file, long lessonId, long userId) {
        try {
            HttpEntity<MultiValueMap<String, Object>> request = buildRequest(file, lessonId, userId);
            ResponseEntity<FileUploadResponse> response = sendRequest(request);

            return extractBody(response);
        } catch (IOException | RestClientException e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    private HttpEntity<MultiValueMap<String, Object>> buildRequest(MultipartFile file, long lessonId, long userId) throws IOException {
        MultiValueMap<String, Object> body = createBody(file, lessonId, userId);
        HttpHeaders headers = createHeader();

        return new HttpEntity<>(body, headers);
    }

    private ResponseEntity<FileUploadResponse> sendRequest( HttpEntity<MultiValueMap<String, Object>> request) {
        return restTemplate.postForEntity(FILE_UPLOAD_URL, request, FileUploadResponse.class);
    }

    private FileUploadResponse extractBody(ResponseEntity<FileUploadResponse> response) throws FileUploadException {
        if(!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new FileUploadException("Failed to upload file");
        }

        return response.getBody();
    }

    private MultiValueMap<String, Object> createBody(MultipartFile file, long lessonId, long userId) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        body.add("file", toResource(file));
        body.add("lessonId", lessonId);
        body.add("userId", userId);

        return body;
    }
    private ByteArrayResource toResource(MultipartFile file) throws IOException {
        return new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };
    }

    private HttpHeaders createHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return headers;
    }

    public void deleteFile(String key) {
        restTemplate.delete(FILE_UPLOAD_URL + "/" + key);
    }

//    public void deleteByKey(String key) {
//        restTemplate.postForEntity("http://localhost:8081/files/{key}", key, String.class);
//    }
}
