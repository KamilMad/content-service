package pl.kamil.content_service.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestClientException;
import pl.kamil.content_service.dtos.FileUploadResponse;
import pl.kamil.content_service.dtos.LessonResponse;
import pl.kamil.content_service.exceptions.AccessDeniedException;
import pl.kamil.content_service.exceptions.FileProcessingException;
import pl.kamil.content_service.exceptions.LessonNotFoundException;
import pl.kamil.content_service.models.Lesson;
import pl.kamil.content_service.repositories.LessonRepository;

import java.util.Optional;

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

    @Test
    void shouldThrowException_WhenProblemWithReadingFile() {
        // Given
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello, World!".getBytes()
        );
        long userId = 1L;

        when(lessonFileService.uploadFile(mockFile)).thenThrow(new FileProcessingException("Failed to read file content"));

        // When

        Exception exception = assertThrows(FileProcessingException.class,
                () -> lessonService.createLesson(mockFile, userId));
        //Then
        assertTrue(exception.getMessage().contains("Failed to read file content"));
        verify(lessonRepository, never()).save(any());
        verify(lessonFileService).uploadFile(mockFile);
    }

    @Test
    void shouldSaveLessonWithCorrectUserIdAndS3key() {
        // Given
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello, World!".getBytes());
        long userId = 1L;

        when(lessonFileService.uploadFile(mockFile)).thenReturn(new FileUploadResponse("s3key"));

        // Mock the repository save to return something
        Lesson mockSavedLesson = new Lesson();
        mockSavedLesson.setId(42L);
        when(lessonRepository.save(any(Lesson.class))).thenReturn(mockSavedLesson);

        // When
        lessonService.createLesson(mockFile, userId);

        // Then
        verify(lessonRepository).save(argThat(lesson ->
                lesson.getCreatedBy() == userId &&
                lesson.getS3Key().equals("s3key")));

    }

    @Test
    void shouldSuccessfullyReturnLessonById() {
        // Given
        long lessonId = 1L;
        long userId = 1L;

        Lesson lesson = Lesson.create("title", userId, 100);
        lesson.setId(lessonId);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        // When
        LessonResponse response = lessonService.getById(lessonId, userId);

        // Then
        assertNotNull(response);
        assertEquals(lessonId, response.id());
        verify(lessonRepository).findById(lessonId);
    }

    @Test
    void shouldThrowException_WhenLessonNotFound() {
        long lessonId = 1L;
        long userId = 1L;

        when(lessonRepository.findById(lessonId)).thenThrow(new LessonNotFoundException("Lesson not found"));

        Exception exception = assertThrows(LessonNotFoundException.class,
                () -> lessonService.getById(lessonId, userId));

        assertTrue(exception.getMessage().contains("Lesson not found"));
    }

    @Test
    void shouldThrowException_WhenUserIsNotOwner() {
        // given
        Long lessonId = 1L;
        Long userId = 2L;
        Lesson lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setCreatedBy(99L); // Different owner

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        // expect
        Exception exception = assertThrows(AccessDeniedException.class, () -> lessonService.getById(lessonId, userId));
        assertTrue(exception.getMessage().contains("You do not have permission to access this lesson"));

    }

}
