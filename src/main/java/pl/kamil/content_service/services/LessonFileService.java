package pl.kamil.content_service.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilderFactory;
import pl.kamil.content_service.common.ErrorMessages;
import pl.kamil.content_service.dtos.FileUploadResponse;
import pl.kamil.content_service.exceptions.FileProcessingException;
import pl.kamil.content_service.exceptions.FileStorageException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonFileService {

    private final RestTemplate restTemplate;

    @Value("${file.upload.url}")
    private String FILE_UPLOAD_URL;

    public FileUploadResponse uploadFile(MultipartFile file) {
        String url = buildUploadUrl();
        HttpEntity<MultiValueMap<String, Object>> request = createMultipartRequest(file);
     try {
         ResponseEntity<FileUploadResponse> response = restTemplate.postForEntity(url, request, FileUploadResponse.class);

         if (response.getBody() == null || !response.getStatusCode().is2xxSuccessful()) {
             throw new FileStorageException(ErrorMessages.FILE_STORAGE_RESPONSE_INVALID);
         }

         return response.getBody();

     } catch (RestClientException e) {
        throw new FileStorageException(ErrorMessages.FILE_UPLOAD_FAILED, e);
     }

    }

    public void deleteFile(String key) {
        try {
            restTemplate.delete(FILE_UPLOAD_URL + "/" + key);
        } catch (RestClientException e) {
            throw new FileStorageException(String.format(ErrorMessages.FILE_DELETE_FAILED, key), e);
        }
    }

    public String getFileContent(String fileKey) {
        Resource fileResource = fetchFileResource(fileKey);
        return readFileContent(fileResource, fileKey);
    }

    private Resource fetchFileResource(String fileKey) {
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

        return response.getBody();
    }

    private String readFileContent(Resource resource, String fileKey) {
        try (InputStream is = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            return reader.lines().collect(Collectors.joining("\n"));

        } catch (RestClientException | IOException e) {
            throw new FileStorageException(String.format(ErrorMessages.FILE_CONTENT_FETCH_FAILED, fileKey), e);
        }
    }

    private String buildUploadUrl() {
        UriBuilderFactory factory = new DefaultUriBuilderFactory(FILE_UPLOAD_URL);
        return factory.builder().build().toString();
    }

    private HttpEntity<MultiValueMap<String, Object>> createMultipartRequest(MultipartFile file) {

        // Prepare the body
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        try {
            HttpHeaders filePartHeaders = new HttpHeaders();

            filePartHeaders.setContentDisposition(ContentDisposition
                    .builder("form-data")
                    .name("file")
                    .filename(file.getOriginalFilename())
                    .build());

            filePartHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            HttpEntity<byte[]> filePart = new HttpEntity<>(file.getBytes(), filePartHeaders);
            body.add("file", filePart);
        } catch (IOException e) {
            throw new FileProcessingException("Failed to read file content", e);
        }

        return new HttpEntity<>(body, headers);
    }

}

