package pl.kamil.content_service.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import pl.kamil.content_service.common.ErrorMessages;
import pl.kamil.content_service.dtos.FileUploadResponse;
import pl.kamil.content_service.dtos.LessonResponse;
import pl.kamil.content_service.dtos.LessonsResponse;
import pl.kamil.content_service.exceptions.AccessDeniedException;
import pl.kamil.content_service.exceptions.FileProcessingException;
import pl.kamil.content_service.exceptions.LessonNotFoundException;
import pl.kamil.content_service.models.Lesson;
import pl.kamil.content_service.repositories.LessonRepository;
import pl.kamil.content_service.util.FileValidator;


import java.util.Collections;
import java.util.List;
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

    @Mock
    private FileValidator fileValidator;

    @InjectMocks
    private LessonService lessonService;

    @Test
    void shouldUploadLessonAndReturnId() {
        // Given
        MultipartFile mockFile = new MockMultipartFile(
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

        when(lessonFileService.uploadFile(mockFile)).thenReturn(response);
        when(lessonRepository.save(any(Lesson.class))).thenReturn(mockLesson);

        // Then
        Long lessonId = lessonService.createLesson(mockFile, userId);

        assertEquals(42L, lessonId);
        verify(lessonFileService).uploadFile(mockFile);
        verify(lessonRepository).save(any(Lesson.class));

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
        when(lessonFileService.uploadFile(mockFile))
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

    @Test
    void shouldSuccessfullyGetAllLessonsForProvidedUserId() {
        // Given
        long userId = 1L;
        Lesson lesson1 =  Lesson.create("title1", userId, 100);
        lesson1.setS3Key("s3Key");
        Lesson lesson2 =  Lesson.create("title2", userId, 200);
        lesson2.setS3Key("s3Key");
        List<Lesson> lessons = List.of(lesson1, lesson2);

        when(lessonRepository.findAllByCreatedBy(userId)).thenReturn(lessons);

        // When
        LessonsResponse response = lessonService.getAll(userId);

        // Then
        assertEquals(response.total(), lessons.size());

        assertEquals(lessons.get(0).getTitle(), response.lessons().get(0).title());
        assertEquals(lessons.get(0).getTotal_words(), response.lessons().get(0).totalWords());
        assertEquals(lessons.get(0).getS3Key(), response.lessons().get(0).s3Key());


        assertEquals(lessons.get(1).getTitle(), response.lessons().get(1).title());
        assertEquals(lessons.get(1).getTotal_words(), response.lessons().get(1).totalWords());
        assertEquals(lessons.get(1).getS3Key(), response.lessons().get(1).s3Key());

    }

    @Test
    void shouldReturnEmptyList_WhenNoLessonExist() {
        // Given
        long userId = 1L;
        when(lessonRepository.findAllByCreatedBy(userId)).thenReturn(Collections.emptyList());

        // When
        LessonsResponse response = lessonService.getAll(userId);

        // Then
        assertNotNull(response);
        assertTrue(response.lessons().isEmpty());
        assertEquals(0, response.total());
    }

    @Test
    void shouldThrowException_WhenDBFails() {
        long userId = 1L;
        RuntimeException dbException = new RuntimeException("DB error");
        when(lessonRepository.findAllByCreatedBy(userId)).thenThrow(dbException);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> lessonService.getAll(userId));
        assertEquals("DB error", thrown.getMessage());
    }

    @Test
    void shouldDeleteLesson_WhenUserIsOwner() {
        long lessonId = 1L;
        long userId = 100L;
        Lesson lesson = Lesson.create( "title", userId, 100);
        lesson.setId(lessonId);
        lesson.setS3Key("s3Key");
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        lessonService.deleteById(lessonId, userId);

        verify(lessonFileService).deleteFile("s3Key");
        verify(lessonRepository).deleteById(lessonId);
    }

    @Test
    void shouldThrowLessonNotFoundException_WhenLessonDoesNotExist() {
        // Given
        Long lessonId = 1L;
        Long userId = 100L;

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        // When
        Exception ex = assertThrows(LessonNotFoundException.class,
                () -> lessonService.deleteById(lessonId, userId));

        // Then
        assertEquals(ErrorMessages.LESSON_NOT_FOUND, ex.getMessage());
        verifyNoInteractions(lessonFileService);
        verify(lessonRepository,never()).deleteById(lessonId);
    }

    @Test
    void shouldThrowAccessDeniedException_WhenUserIsNotOwner() {
        // Given
        long lessonId = 1L;
        long userId = 100L;
        long wrongUserId = 200L;
        Lesson lesson = Lesson.create( "title", userId, 100);
        lesson.setId(lessonId);
        lesson.setS3Key("s3Key");
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        Exception ex = assertThrows(AccessDeniedException.class,
                () -> lessonService.deleteById(lessonId, wrongUserId));

        assertEquals(ErrorMessages.ACCESS_DENIED, ex.getMessage());
        verifyNoInteractions(lessonFileService);
        verify(lessonRepository, never()).deleteById(lessonId);
    }

    // successfully get lesson content when valid user
    @Test
    void getLessonContent_shouldReturnLessonContent_WhenValidOwner() {
        long lessonId = 1L;
        long userId = 100L;
        Lesson lesson = Lesson.create( "title", userId, 100);
        lesson.setId(lessonId);
        lesson.setS3Key("s3Key");

        String fileContent = " =File content";

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonFileService.getFileContent("s3Key")).thenReturn(fileContent);

        String content = lessonService.getLessonContent(lessonId, userId);

        assertEquals(fileContent, content);
        verify(lessonRepository).findById(lessonId);
        verify(lessonFileService).getFileContent("s3Key");

    }
    // throw exception when invalid user
    @Test
    void getLessonContent_shouldThrowException_WhenInvalidOwner() {
        // Given
        long lessonId = 1L;
        long ownerUserId = 100L;
        Lesson lesson = Lesson.create( "title", ownerUserId, 100);
        lesson.setId(lessonId);
        lesson.setS3Key("s3Key");

        long wrongUserId = 200L;
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        // When
        Exception ex = assertThrows(AccessDeniedException.class,
                () -> lessonService.getLessonContent(lessonId, wrongUserId));

        // Then
        assertEquals(ErrorMessages.ACCESS_DENIED, ex.getMessage());
        verify(lessonFileService, never()).getFileContent(anyString());
    }

    @Test
    void getLessonContent_shouldThrowException_WhenLessonNotFound() {
        long lessonId = 1L;
        long userId = 100L;
        when(lessonRepository.findById(any())).thenReturn(Optional.empty());

        Exception ex = assertThrows(LessonNotFoundException.class,
                () -> lessonService.getLessonContent(lessonId, userId));

        assertEquals(ErrorMessages.LESSON_NOT_FOUND, ex.getMessage());
        verify(lessonFileService, never()).getFileContent(anyString());

    }
    // throw exception lesson not found
}
