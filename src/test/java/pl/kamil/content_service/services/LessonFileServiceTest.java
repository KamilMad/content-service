package pl.kamil.content_service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.web.multipart.MultipartFile;
import pl.kamil.content_service.common.ErrorMessages;
import pl.kamil.content_service.dtos.FileUploadResponse;
import pl.kamil.content_service.exceptions.ApiError;
import pl.kamil.content_service.exceptions.FileStorageException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RestClientTest(LessonFileService.class)
public class LessonFileServiceTest {

    @Autowired
    private LessonFileService lessonFileService;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

    private MultipartFile mockFile;
    private FileUploadResponse successfulResponse;
    private final String uploadUrl = "http://localhost:8083/files"; // Example URL from the service

    @BeforeEach
    void setUp() {
        mockFile = new MockMultipartFile("file", "test-file.txt", MediaType.TEXT_PLAIN_VALUE, "file content".getBytes());
        successfulResponse = new FileUploadResponse("s3Key");
    }
//    @Test
//    void uploadFile_ShouldUploadFileAndReturnFileUploadResponse() throws JsonProcessingException {
//        // Given
//        String expectedResponseJson = objectMapper.writeValueAsString(successfulResponse);
//
//        server.expect(requestTo(uploadUrl))
//                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
//                .andRespond(withStatus(HttpStatus.OK)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .body(expectedResponseJson));
//
//        // When
//        FileUploadResponse result = lessonFileService.uploadFile(mockFile);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(successfulResponse.s3Key(), result.s3Key());
//        server.verify();
//    }

    @Test
    void uploadFile_ShouldReturnBadGateway_WhenStatusCodeIsNot2xx() throws JsonProcessingException {
        ApiError error = new ApiError(
                HttpStatus.BAD_GATEWAY.value(),
                ErrorMessages.FILE_STORAGE_RESPONSE_INVALID,
                uploadUrl
        );

        String errorJson = objectMapper.writeValueAsString(error);

        server.expect(requestTo(uploadUrl))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorJson));

        FileStorageException exception = assertThrows(FileStorageException.class, () -> lessonFileService.uploadFile(mockFile));
        assertEquals(ErrorMessages.FILE_STORAGE_RESPONSE_INVALID, exception.getMessage());
    }

    @Test
    void uploadFile_ShouldReturnBadGateway_WhenResponseBodyIsNull() throws JsonProcessingException {
        server.expect(requestTo(uploadUrl))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY)
                        .contentType(MediaType.APPLICATION_JSON));

        FileStorageException exception = assertThrows(FileStorageException.class, () -> lessonFileService.uploadFile(mockFile));
        assertEquals(ErrorMessages.FILE_STORAGE_RESPONSE_INVALID, exception.getMessage());
    }
}
