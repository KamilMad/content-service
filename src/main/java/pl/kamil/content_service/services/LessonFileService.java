package pl.kamil.content_service.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import pl.kamil.content_service.common.ErrorMessages;
import pl.kamil.content_service.dtos.FileUploadResponse;
import pl.kamil.content_service.exceptions.FileStorageException;
import pl.kamil.content_service.exceptions.FileUploadException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonFileService {

    private final RestTemplate restTemplate;
    //private static final String FILE_UPLOAD_URL = "http://localhost:8083/files";

    @Value("${file.upload.url}")
    private String FILE_UPLOAD_URL;

    public FileUploadResponse uploadFile(MultipartFile file, long lessonId, long userId) {
        try {
            HttpEntity<MultiValueMap<String, Object>> request = buildRequest(file, lessonId, userId);
            ResponseEntity<FileUploadResponse> response = sendRequest(request);

            return extractBody(response);
        } catch (IOException | RestClientException e) {
            throw new FileUploadException(ErrorMessages.FILE_UPLOAD_FAILED);
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

    private FileUploadResponse extractBody(ResponseEntity<FileUploadResponse> response) {
        if(!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new FileUploadException(ErrorMessages.FILE_STORAGE_RESPONSE_INVALID);
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
        try{
            restTemplate.delete(FILE_UPLOAD_URL + "/" + key);
        } catch (RestClientException e) {
            throw new FileStorageException(String.format(ErrorMessages.FILE_DELETE_FAILED, key),e);
        }
    }

    public String getFileContent(String fileKey) {

        String url = FILE_UPLOAD_URL + "/" + fileKey;

        ResponseEntity<Resource> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                Resource.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new FileStorageException(ErrorMessages.FILE_STORAGE_RESPONSE_INVALID);
        }

        try (InputStream is = response.getBody().getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            return reader.lines().collect(Collectors.joining("\n"));

        } catch (RestClientException | IOException e) {
            throw new FileStorageException(String.format(ErrorMessages.FILE_CONTENT_FETCH_FAILED ,fileKey), e);
        }
    }

}
