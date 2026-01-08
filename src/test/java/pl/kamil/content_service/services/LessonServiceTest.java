package pl.kamil.content_service.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import pl.kamil.content_service.common.ErrorMessages;
import pl.kamil.content_service.dtos.FileUploadResponse;
import pl.kamil.content_service.dtos.LessonContentResponse;
import pl.kamil.content_service.dtos.LessonResponse;
import pl.kamil.content_service.dtos.LessonsResponse;
import pl.kamil.content_service.exceptions.FileProcessingException;
import pl.kamil.content_service.exceptions.ForbiddenAccessException;
import pl.kamil.content_service.exceptions.ResourceNotFoundException;
import pl.kamil.content_service.models.Lesson;
import pl.kamil.content_service.repositories.LessonRepository;
import pl.kamil.content_service.utils.LessonFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private FileStorageClient fileStorageClient;

    @Mock
    private ContentService contentService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private LessonService lessonService;

    private final UUID lessonId = LessonFactory.TEST_LESSON_ID;
    private final UUID userId = LessonFactory.TEST_USER_ID;
    private final UUID otherUserId = LessonFactory.OTHER_TEST_USER_ID;


    @Test
    void shouldUploadLessonAndReturnId() {
        // Given
        MultipartFile mockFile = LessonFactory.createMockFile();

        FileUploadResponse response = new FileUploadResponse(LessonFactory.DEFAULT_S3_KEY);
        Lesson lesson = LessonFactory.createLessonWithContent();

        // When
        when(fileStorageClient.storeFile(mockFile)).thenReturn(response);
        when(lessonRepository.save(any(Lesson.class))).thenReturn(lesson);

        // Then
        LessonResponse lessonResponse= lessonService.createLesson(mockFile, userId);

        assertEquals(lessonId, lessonResponse.id());
        verify(fileStorageClient).storeFile(mockFile);
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
        when(fileStorageClient.storeFile(mockFile))
                .thenThrow(new RestClientException("Invalid response from file storage service"));

        // When
        Exception exception = assertThrows(RestClientException.class, () -> lessonService.createLesson(mockFile, userId));

        // Then
        assertTrue(exception.getMessage().contains("Invalid response from file storage service"));
        verify(lessonRepository, never()).save(any());
        verify(fileStorageClient).storeFile(mockFile);
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
        

        when(fileStorageClient.storeFile(mockFile)).thenThrow(new FileProcessingException("Failed to read file content"));

        // When

        Exception exception = assertThrows(FileProcessingException.class,
                () -> lessonService.createLesson(mockFile, userId));
        //Then
        assertTrue(exception.getMessage().contains("Failed to read file content"));
        verify(lessonRepository, never()).save(any());
        verify(fileStorageClient).storeFile(mockFile);
    }

//    @Test
//    void shouldSaveLessonWithCorrectUserIdAndS3key() {
//        // Given
//        MockMultipartFile mockFile = new MockMultipartFile(
//                "file",
//                "test.txt",
//                "text/plain",
//                "Hello, World!".getBytes());
//
//
//        when(lessonFileService.storeFile(mockFile)).thenReturn(new FileUploadResponse("s3key"));
//
//        // Mock the repository save to return something
//        Lesson mockSavedLesson = new Lesson();
//        mockSavedLesson.setId(42L);
//        when(lessonRepository.save(any(Lesson.class))).thenReturn(mockSavedLesson);
//
//        // When
//        lessonService.createLesson(mockFile, userId);
//
//        // Then
//        verify(lessonRepository).save(argThat(lesson ->
//                lesson.getCreatedBy() == userId &&
//                        lesson.getS3Key().equals("s3key")));
//
//    }

    @Test
    void shouldSuccessfullyReturnLessonById() {
        // Given
        Lesson lesson = LessonFactory.createLesson();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        // When
        LessonResponse response = lessonService.getLesson(lessonId, userId);

        // Then
        assertNotNull(response);
        assertEquals(lessonId, response.id());
        verify(lessonRepository).findById(lessonId);
    }

    @Test
    void shouldThrowException_WhenLessonNotFound() {
        
        

        when(lessonRepository.findById(lessonId)).thenThrow(new ResourceNotFoundException("Lesson not found"));

        Exception exception = assertThrows(ResourceNotFoundException.class,
                () -> lessonService.getLesson(lessonId, userId));

        assertTrue(exception.getMessage().contains("Lesson not found"));
    }

    @Test
    void shouldThrowException_WhenUserIsNotOwner() {
        // given
        Lesson lesson = LessonFactory.createLesson();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        // expect
        Exception exception = assertThrows(ForbiddenAccessException.class, () -> lessonService.getLesson(lessonId, otherUserId));
        assertTrue(exception.getMessage().contains("You do not have permission to access this lesson"));

    }

    @Test
    void shouldSuccessfullyGetAllLessonsForProvidedUserId() {
        // Given
        List<Lesson> lessons = LessonFactory.createLessonList(3);
        when(lessonRepository.findAllByCreatedBy(userId)).thenReturn(lessons);

        // When
        LessonsResponse response = lessonService.getAllLessons(userId);

        // Then
        assertEquals(response.total(), lessons.size());

        assertEquals(lessons.get(0).getTitle(), response.lessons().get(0).title());
        assertEquals(lessons.get(1).getTitle(), response.lessons().get(1).title());

    }

    @Test
    void shouldReturnEmptyList_WhenNoLessonExist() {
        // Given
        
        when(lessonRepository.findAllByCreatedBy(userId)).thenReturn(Collections.emptyList());

        // When
        LessonsResponse response = lessonService.getAllLessons(userId);

        // Then
        assertNotNull(response);
        assertTrue(response.lessons().isEmpty());
        assertEquals(0, response.total());
    }

    @Test
    void shouldThrowException_WhenDBFails() {
        
        RuntimeException dbException = new RuntimeException("DB error");
        when(lessonRepository.findAllByCreatedBy(userId)).thenThrow(dbException);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> lessonService.getAllLessons(userId));
        assertEquals("DB error", thrown.getMessage());
    }

    @Test
    void shouldDeleteLesson_WhenUserIsOwner() {

        Lesson lesson = LessonFactory.createLessonWithContent();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        lessonService.deleteLesson(lessonId, userId);

        verify(lessonRepository).delete(lesson);

    }

    @Test
    void shouldThrowResourceNotFoundException_WhenLessonDoesNotExist() {
        // Given
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        // When
        Exception ex = assertThrows(ResourceNotFoundException.class,
                () -> lessonService.deleteLesson(lessonId, userId));

        // Then
        assertEquals(ErrorMessages.LESSON_NOT_FOUND, ex.getMessage());
        verifyNoInteractions(fileStorageClient);
        verify(lessonRepository, never()).deleteById(lessonId);
    }

    @Test
    void shouldThrowAccessDeniedException_WhenUserIsNotOwner() {
        // Given
        long wrongUserId = 200L;
        Lesson lesson = LessonFactory.createLesson();
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        Exception ex = assertThrows(ForbiddenAccessException.class,
                () -> lessonService.deleteLesson(lessonId, otherUserId));

        assertEquals(ErrorMessages.ACCESS_DENIED, ex.getMessage());
        verifyNoInteractions(fileStorageClient);
        verify(lessonRepository, never()).deleteById(lessonId);
    }

    // successfully get lesson content when valid user
    @Test
    void getLessonContent_shouldReturnLessonContent_WhenValidOwner() {

        Lesson lesson = LessonFactory.createLessonWithContent();
        LessonContentResponse expectedResponse = LessonFactory.createLessonContentResponse();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(fileStorageClient.getFileContent(LessonFactory.DEFAULT_S3_KEY))
                .thenReturn(LessonFactory.DEFAULT_FILE_CONTENT);

        LessonContentResponse actualResponse = lessonService
                .getLessonContent(lessonId, userId);

        assertEquals(expectedResponse, actualResponse);
        verify(lessonRepository).findById(lessonId);
        verify(fileStorageClient).getFileContent(LessonFactory.DEFAULT_S3_KEY);

    }

    // throw exception when invalid user
    @Test
    void getLessonContent_shouldThrowException_WhenInvalidOwner() {
        // Given
        Lesson lesson = LessonFactory.createLessonWithContent();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        // When
        Exception ex = assertThrows(ForbiddenAccessException.class,
                () -> lessonService.getLessonContent(lessonId, otherUserId));

        // Then
        assertEquals(ErrorMessages.ACCESS_DENIED, ex.getMessage());
        verify(fileStorageClient, never()).getFileContent(anyString());
    }

    @Test
    void getLessonContent_shouldThrowException_WhenLessonNotFound() {

        when(lessonRepository.findById(any())).thenReturn(Optional.empty());

        Exception ex = assertThrows(ResourceNotFoundException.class,
                () -> lessonService.getLessonContent(lessonId, userId));

        assertEquals(ErrorMessages.LESSON_NOT_FOUND, ex.getMessage());
        verify(fileStorageClient, never()).getFileContent(anyString());

    }
    // throw exception lesson not found
}
