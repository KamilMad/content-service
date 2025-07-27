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
import pl.kamil.content_service.services.LessonService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
}
