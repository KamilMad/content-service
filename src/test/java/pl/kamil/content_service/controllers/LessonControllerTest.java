package pl.kamil.content_service.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import pl.kamil.content_service.dtos.LessonResponse;
import pl.kamil.content_service.dtos.LessonsResponse;
import pl.kamil.content_service.exceptions.FileProcessingException;
import pl.kamil.content_service.models.Lesson;
import pl.kamil.content_service.services.LessonService;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
public class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LessonService lessonService;

    @Test
    void shouldCreateLessonAndReturnId() throws Exception {
        // Given
        long userId = 1L;
        Long lessonId = 42L;

        MockMultipartFile file = new MockMultipartFile(
                "file", "lesson.txt", "text/plain", "Sample content".getBytes()
        );

        when(lessonService.createLesson(file, userId)).thenReturn(lessonId);

        mockMvc.perform(multipart("/lessons")
                .file(file)
                .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("42"));

        verify(lessonService).createLesson(file, userId);
    }

    @Test
    void shouldReturnBadRequest_WhenFileIsMissing() throws Exception {
        long userId = 1L;
        mockMvc.perform(multipart("/lessons")
                .header("X-User-Id", userId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_WhenFileIsEmpty() throws Exception {

        long userId = 1L;
        MockMultipartFile file = new MockMultipartFile("file",
                "lesson.txt",
                "text/plain",
                new byte[0]);

        mockMvc.perform(multipart("/lessons")
                        .file(file)
                        .header("X-User-Id", userId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_WhenUserIdHeaderIsMissing() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "lesson.txt", "text/plain", "Sample content".getBytes()
        );
        mockMvc.perform(multipart("/lessons")
                .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Required request header 'X-User-Id' for method parameter type Long is not present"));
    }

    @Test
    void shouldThrowFileProcessingException_WhenContentTypeIsUnsupported() throws Exception {
        long userId = 1L;

        MockMultipartFile mockMultipartFile = new MockMultipartFile("file",
                "lesson.txt",
                MediaType.APPLICATION_JSON_VALUE,
                "Sample content".getBytes()
                );

        when(lessonService.createLesson(mockMultipartFile, userId))
                .thenThrow(new FileProcessingException("Unsupported file type: " + mockMultipartFile.getContentType()));

        mockMvc.perform(multipart("/lessons")
                .file(mockMultipartFile)
                        .header("X-User-Id", userId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unsupported file type: " + mockMultipartFile.getContentType()));
    }

    @Test
    void shouldThrowFileProcessingException_WhenFileSizeIsToBig() throws Exception {
       long userId = 1L;
       byte[] largeContent = new byte[2 * 1024 * 1024]; // 2MB
        Arrays.fill(largeContent, (byte)'A');

        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "file",
                "large_file.txt",
                MediaType.TEXT_PLAIN_VALUE,
                largeContent
        );

        when(lessonService.createLesson(mockMultipartFile, userId))
                .thenThrow(new FileProcessingException("File exceeds the maximum size 1MB"));

        mockMvc.perform(multipart("/lessons")
                .file(mockMultipartFile)
                .header("X-User-Id", userId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("File exceeds the maximum size 1MB"));
    }

    @Test
    void getLessons_shouldReturnLessonsWhenUserIdHeaderIsPresent() throws Exception {
        // Given
        Long userId = 1L;

        List<LessonResponse> lessonList = List.of(
                new LessonResponse(1L, "Lesson 1", 100, Instant.now(), "S3Key"),
                new LessonResponse(2L, "Lesson 2", 200, Instant.now(), "S3Key")
        );

        LessonsResponse expectedResponse = new LessonsResponse(lessonList, lessonList.size());

        when(lessonService.getAll(userId)).thenReturn(expectedResponse);

        mockMvc.perform(get("/lessons")
                .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lessons.length()").value(2))
                .andExpect(jsonPath("$.lessons[0].title").value("Lesson 1"))
                .andExpect(jsonPath("$.lessons[1].title").value("Lesson 2"))
                .andExpect(jsonPath("$.total").value(2));
    }

    @Test
    void getLessons_ShouldReturnBadRequest_WhenUserIdHeaderIsMissing() throws Exception {

        mockMvc.perform(get("/lessons"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Required request header 'X-User-Id' for method parameter type Long is not present"));
    }

    @Test
    void getLessons_ShouldReturnEmptyList_WhenUserHasNoLessons() throws Exception {
        long userId = 1L;
        LessonsResponse response = new LessonsResponse(Collections.emptyList(), 0);

        when(lessonService.getAll(userId)).thenReturn(response);

        mockMvc.perform(get("/lessons")
                .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lessons.length()").value(0));
    }

    @Test
    void getLessons_ShouldReturn500WhenServiceThrowsException() throws Exception {
        long userId = 1L;
        when(lessonService.getAll(userId)).thenThrow(new RuntimeException("Something went wrong"));

        mockMvc.perform(get("/lessons")
                .header("X-User-Id", userId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Something went wrong"));


    }
}
