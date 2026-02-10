package pl.kamil.content_service.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import pl.kamil.content_service.common.ErrorMessages;
import pl.kamil.content_service.dtos.FileUploadResponse;
import pl.kamil.content_service.dtos.LessonContentResponse;
import pl.kamil.content_service.dtos.LessonResponse;
import pl.kamil.content_service.dtos.PagedResponse;
import pl.kamil.content_service.exceptions.FileProcessingException;
import pl.kamil.content_service.exceptions.ForbiddenAccessException;
import pl.kamil.content_service.exceptions.ResourceNotFoundException;
import pl.kamil.content_service.models.Lesson;
import pl.kamil.content_service.repositories.LessonRepository;
import pl.kamil.content_service.utils.LessonFactory;

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

    @Test
    void shouldSaveLessonWithCorrectData() {
        // Given
        MockMultipartFile mockFile = LessonFactory.createMockFile();

        Lesson mockSavedLesson = LessonFactory.createLessonWithContent();

        when(fileStorageClient.storeFile(mockFile)).thenReturn(new FileUploadResponse(LessonFactory.DEFAULT_S3_KEY));
        when(lessonRepository.save(any(Lesson.class))).thenReturn(mockSavedLesson);

        // When
        lessonService.createLesson(mockFile, userId);

        verify(lessonRepository).save(argThat(lesson ->
                lesson.getCreatedBy().equals(userId) &&
                lesson.getTitle().equals(mockSavedLesson.getTitle())));
    }

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
        int pageNo = 0;
        int pageSize = 10;
        List<Lesson> lessons = LessonFactory.createLessonList(3);
        Page<Lesson> lessonPage = new PageImpl<>(lessons, PageRequest.of(pageNo, pageSize), 3);

        when(lessonRepository.findAllByCreatedBy(eq(userId), any(Pageable.class)))
                .thenReturn(lessonPage);

        // When
        PagedResponse response = lessonService.getAllLessons(userId, pageNo, pageSize);

        // Then
        assertNotNull(response);
        assertEquals(3, response.content().size());
        assertEquals(pageNo, response.pageNo());
        assertEquals(pageSize, response.pageSize());
        assertEquals(3, response.totalElements());
        assertEquals(1, response.totalPages());
        assertTrue(response.last());

        verify(lessonRepository).findAllByCreatedBy(eq(userId), any(Pageable.class));

    }

    @Test
    void shouldReturnEmptyList_WhenNoLessonExist() {
        // Given
        int pageNo = 0;
        int pageSize = 10;
        Page<Lesson> emptyPage = new PageImpl<>(List.of(), PageRequest.of(pageNo, pageSize), 0);

        when(lessonRepository.findAllByCreatedBy(eq(userId), any(Pageable.class)))
                .thenReturn(emptyPage);

        // When
        PagedResponse response = lessonService.getAllLessons(userId, pageNo, pageSize);

        // Then
        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertEquals(0, response.pageNo());
        assertEquals(10, response.pageSize());
        assertEquals(0, response.totalElements());
        assertTrue(response.last());
    }

    @Test
    void shouldThrowException_WhenDBFails() {
        int pageNo = 0;
        int pageSize = 10;
        RuntimeException dbException = new RuntimeException("DB error");
        when(lessonRepository.findAllByCreatedBy(eq(userId), any(Pageable.class))).thenThrow(dbException);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> lessonService.getAllLessons(userId, pageNo, pageSize));
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
