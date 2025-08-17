package pl.kamil.content_service.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
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
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class LessonFileService {

    private final RestClient restClient;

    @Value("${file.upload.url}")
    private String FILE_UPLOAD_URL;

    public LessonFileService(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public FileUploadResponse uploadFile(MultipartFile file) {
        MultipartBodyBuilder multipartBodyBuilder = createMultipartBodybuilder(file);
        String url = buildUploadUrl();

        try {
            return restClient.post()
                    .uri(url)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(multipartBodyBuilder.build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new FileStorageException("Upload failed with status: " + response.getStatusCode());
                    })
                    .body(FileUploadResponse.class);
        }catch (RestClientException e) {
            throw new FileStorageException(ErrorMessages.FILE_DECODE_FAILED);
        }
    }

    public void deleteFile(String key) {
        try {
            restClient.delete()
                    .uri(FILE_UPLOAD_URL + "/" + key)
                    .retrieve();
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

        ResponseEntity<Resource> response =  restClient.get()
                .uri(url)
                .retrieve()
                .toEntity(Resource.class);

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

    private MultipartBodyBuilder createMultipartBodybuilder(MultipartFile file) {
        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("file", file.getResource())  // ✅ Pass as Resource, not bytes
                .filename(file.getOriginalFilename())
                .contentType(file.getContentType() != null
                        ? MediaType.parseMediaType(file.getContentType())
                        : MediaType.APPLICATION_OCTET_STREAM);
        return multipartBodyBuilder;
    }

    private HttpEntity<byte[]> createFilePart(MultipartFile file) {
        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentDisposition(ContentDisposition
                .builder("form-data")
                .name("file")
                .filename(file.getOriginalFilename())
                .build());

        fileHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        try {
            return new HttpEntity<>(file.getBytes(), fileHeaders);
        }catch (IOException e) {
            throw new FileProcessingException("Failed to read file content", e);
        }
    }
}

