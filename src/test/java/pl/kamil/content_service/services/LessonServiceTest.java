package pl.kamil.content_service.services;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import pl.kamil.content_service.dtos.FileUploadResponse;
import pl.kamil.content_service.models.Lesson;
import pl.kamil.content_service.repositories.LessonRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class LessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private LessonFileService lessonFileService;

    @Spy
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

    // Invalid or null file. Should thrown
}
