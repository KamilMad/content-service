package pl.kamil.content_service.services;


import jakarta.validation.constraints.AssertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestClientException;
import pl.kamil.content_service.dtos.FileUploadResponse;
import pl.kamil.content_service.models.Lesson;
import pl.kamil.content_service.repositories.LessonRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private LessonFileService lessonFileService;

    @InjectMocks
    private LessonService lessonService;

    @Test
    void shouldUploadLessonAndReturnId() {
        // Given
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello, World!".getBytes()
        );
        long userId = 1L;

        FileUploadResponse response = new FileUploadResponse("s3key");
        Lesson mockLesson = new Lesson();
        mockLesson.setId(42L);

        // When
        Mockito.when(lessonFileService.uploadFile(mockFile)).thenReturn(response);
        Mockito.when(lessonRepository.save(any(Lesson.class))).thenReturn(mockLesson);

        // Then
        Long lessonId = lessonService.createLesson(mockFile, userId);

        assertEquals(42L, lessonId);
        verify(lessonFileService).uploadFile(mockFile);
        verify(lessonRepository).save(any(Lesson.class));

    }

    // Invalid or null file. Should have thrown
    @Test
    void shouldThrowIllegalArgumentException_WhenFileIsNull() {
        MockMultipartFile mockFile = null;

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> lessonService.createLesson(mockFile, 1L));

        assertTrue(exception.getMessage().contains("File must not be null or empty"));
    }

    @Test
    void shouldThrowException_WhenFileIsEmpty() {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "name.txt",
                "text/plain",
                new byte[0]
        );

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> lessonService.createLesson(mockFile, 1L));

        assertTrue(exception.getMessage().contains("File must not be null or empty"));
    }

    @Test
    void shouldThrowException_WhenProblemWithUploading() {
        // Given
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello, World!".getBytes()
        );
        long userId = 1L;
        when(lessonFileService.uploadFile(any(MockMultipartFile.class)))
                .thenThrow(new RestClientException("Invalid response from file storage service"));

        // When
        Exception exception = assertThrows(RestClientException.class, () -> lessonService.createLesson(mockFile, userId));

        // Then
        assertTrue(exception.getMessage().contains("Invalid response from file storage service"));
        verify(lessonRepository, never()).save(any());
        verify(lessonFileService).uploadFile(mockFile);
    }


}
