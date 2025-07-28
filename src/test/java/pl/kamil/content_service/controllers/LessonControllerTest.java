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
import pl.kamil.content_service.exceptions.FileProcessingException;
import pl.kamil.content_service.services.LessonService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                .andExpect(status().isBadRequest());
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


}
