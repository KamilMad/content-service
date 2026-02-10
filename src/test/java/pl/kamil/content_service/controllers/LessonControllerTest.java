package pl.kamil.content_service.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import pl.kamil.content_service.common.ErrorMessages;
import pl.kamil.content_service.dtos.LessonResponse;
import pl.kamil.content_service.dtos.PagedResponse;
import pl.kamil.content_service.exceptions.ForbiddenAccessException;
import pl.kamil.content_service.exceptions.ResourceNotFoundException;
import pl.kamil.content_service.models.Content;
import pl.kamil.content_service.models.Lesson;
import pl.kamil.content_service.services.LessonService;
import pl.kamil.content_service.utils.LessonFactory;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
public class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LessonService lessonService;

    private final UUID lessonId = LessonFactory.TEST_LESSON_ID;
    private final UUID userId = LessonFactory.TEST_USER_ID;

    @Test
    void shouldCreateLessonAndReturnId() throws Exception {
        // Given
        MockMultipartFile file = LessonFactory.createMockFile();
        LessonResponse lessonResponse  = LessonFactory.createLessonResponse();

        when(lessonService.createLesson(file, userId)).thenReturn(lessonResponse);

        mockMvc.perform(multipart("/content")
                .file(file)
                .header("X-User-Id", userId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(lessonResponse.id().toString()))
                .andExpect(jsonPath("$.title").value(lessonResponse.title()))
                .andExpect(jsonPath("$.createdAt").value(lessonResponse.createdAt().toString()))
                .andExpect(jsonPath("$.updatedAt").value(lessonResponse.updatedAt().toString()));

        verify(lessonService).createLesson(file, userId);
    }

    @Test
    void shouldReturnBadRequest_WhenFileIsMissing() throws Exception {
        mockMvc.perform(multipart("/content")
                .header("X-User-Id", userId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_WhenFileIsEmpty() throws Exception {
        
        MockMultipartFile file = LessonFactory
                .createCustomMockFile(null, null, new byte[0]);

        mockMvc.perform(multipart("/content")
                        .file(file)
                        .header("X-User-Id", userId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_WhenUserIdHeaderIsMissing() throws Exception {
        MockMultipartFile file = LessonFactory.createMockFile();

        mockMvc.perform(multipart("/content")
                .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Required request header 'X-User-Id' for method parameter type UUID is not present"));
    }

    @Test
    void shouldThrowFileProcessingException_WhenContentTypeIsUnsupported() throws Exception {

        MockMultipartFile mockMultipartFile = LessonFactory
                .createCustomMockFile(null, MediaType.APPLICATION_JSON_VALUE, null);

        mockMvc.perform(multipart("/content")
                .file(mockMultipartFile)
                        .header("X-User-Id", userId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.file").value("Invalid content type"));
    }

    @Test
    void shouldThrowFileProcessingException_WhenFileSizeIsToBig() throws Exception {

        byte[] largeContent = new byte[2 * 1024 * 1024]; // 2MB
        Arrays.fill(largeContent, (byte)'A');

        MockMultipartFile mockMultipartFile = LessonFactory
                .createCustomMockFile(null, null, largeContent);

        mockMvc.perform(multipart("/content")
                .file(mockMultipartFile)
                .header("X-User-Id", userId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.file").value("File size exceeds the maximum allowed size"));
    }

    @Test
    void getLessons_shouldReturnLessonsWhenUserIdHeaderIsPresent() throws Exception {
        // Given
        

        List<LessonResponse> lessonList = List.of(
                new LessonResponse(UUID.randomUUID(), "Lesson 1", Instant.now(), Instant.now()),
                new LessonResponse(UUID.randomUUID(), "Lesson 2", Instant.now(), Instant.now())
        );

        PagedResponse expectedResponse = new PagedResponse(lessonList, 0, 10, 2, 1, true);

        when(lessonService.getAllLessons(userId, 0, 10)).thenReturn(expectedResponse);

        mockMvc.perform(get("/content")
                .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("Lesson 1"))
                .andExpect(jsonPath("$.content[1].title").value("Lesson 2"))
                .andExpect(jsonPath("$.total").value(2));
    }

    @Test
    void getLessons_ShouldReturnBadRequest_WhenUserIdHeaderIsMissing() throws Exception {

        mockMvc.perform(get("/content"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Required request header 'X-User-Id' for method parameter type UUID is not present"));
    }

//    @Test
//    void getLessons_ShouldReturnEmptyList_WhenUserHasNoLessons() throws Exception {
//
//        LessonsResponse response = new LessonsResponse(Collections.emptyList(), 0);
//
//        when(lessonService.getAllLessons(userId)).thenReturn(response);
//
//        mockMvc.perform(get("/content")
//                .header("X-User-Id", userId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.content.length()").value(0));
//    }
//
//    @Test
//    void getLessons_ShouldReturn500WhenServiceThrowsException() throws Exception {
//
//        when(lessonService.getAllLessons(userId)).thenThrow(new RuntimeException("Something went wrong"));
//
//        mockMvc.perform(get("/content")
//                .header("X-User-Id", userId))
//                .andExpect(status().isInternalServerError())
//                .andExpect(jsonPath("$.message").value("Something went wrong"));
//
//    }

    @Test
    void getLessonById_shouldReturnLessonResponse_whenLessonExistsAndUserIsOwner() throws Exception {
        Content content = new Content();
        Lesson lesson = new Lesson(lessonId, userId, "title", content, Instant.now(), Instant.now());
        LessonResponse lessonResponse = LessonResponse.from(lesson);

        when(lessonService.getLesson(lessonId, userId)).thenReturn(lessonResponse);

        mockMvc.perform(get("/content/{lessonId}", lessonId)
                .header("X-User-Id", userId))
                .andExpect(status().isOk());
    }

    @Test
    void getLessonById_shouldThrowResourceNotFoundException_whenLessonDoesNotExist() throws Exception {

        
        

        when(lessonService.getLesson(lessonId, userId)).thenThrow(new ResourceNotFoundException(ErrorMessages.LESSON_NOT_FOUND));

        mockMvc.perform(get("/content/{lessonId}", lessonId)
                        .header("X-User-Id", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorMessages.LESSON_NOT_FOUND));

        verify(lessonService).getLesson(lessonId, userId);

    }

    @Test
     void getLessonById_shouldThrowAccessDeniedException_whenUserIsNotLessonOwner() throws Exception {
         
         

         when(lessonService.getLesson(lessonId, userId)).thenThrow(new ForbiddenAccessException(ErrorMessages.ACCESS_DENIED));

         mockMvc.perform(get("/content/{lessonId}", lessonId)
                         .header("X-User-Id", userId))
                 .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorMessages.ACCESS_DENIED));

        verify(lessonService).getLesson(lessonId, userId);

    }

    @Test
     void getLessonById_ShouldReturnBadRequest_WhenUserIdHeaderIsMissing() throws Exception {
        mockMvc.perform(get("/content/{userId}", lessonId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Required request header 'X-User-Id' for method parameter type UUID is not present"));
    }


    private ResultActions performDeleteLesson(UUID lessonId, UUID userId) throws Exception {
        return mockMvc.perform(delete("/content/{lessonId}", lessonId)
                .header("X-User-Id", userId));
    }

    @Test
    void shouldDeleteLesson_WhenLessonExistsAndUserIsOwner() throws Exception {
        
        

        performDeleteLesson(lessonId, userId)
                .andExpect(status().isNoContent());

        verify(lessonService).deleteLesson(lessonId, userId);
    }

    @Test
    void deleteLesson_shouldReturnNotFound_whenLessonDoesNotExist() throws Exception {
        
        

        doThrow(new ResourceNotFoundException(ErrorMessages.LESSON_NOT_FOUND))
                .when(lessonService).deleteLesson(lessonId, userId);

        performDeleteLesson(lessonId, userId)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorMessages.LESSON_NOT_FOUND));
    }

    @Test
    void deleteLesson_shouldReturnUnauthorized_whenUserIsNotOwner() throws Exception {
        
        

        doThrow(new ForbiddenAccessException(ErrorMessages.ACCESS_DENIED))
                .when(lessonService).deleteLesson(lessonId, userId);

        performDeleteLesson(lessonId, userId)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorMessages.ACCESS_DENIED));
    }

    private ResultActions performGetLessonContent(UUID lessonId, UUID userId) throws Exception {
        return mockMvc.perform(get("/content/{id}/content", lessonId)
                .header("X-User-Id", userId));
    }

//    @Test
//    void getContent_shouldReturnLessonContent_whenUserOwnsLesson() throws Exception {
//        LessonContentResponse lessonContentResponse = LessonFactory.createLessonContentResponse();
//
//        when(lessonService.getLessonContent(LessonFactory.TEST_LESSON_ID, LessonFactory.TEST_USER_ID))
//                .thenReturn(lessonContentResponse);
//
//        // when & then
//        performGetLessonContent(LessonFactory.TEST_LESSON_ID, LessonFactory.TEST_USER_ID)
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.page").value(LessonFactory.DEFAULT_FILE_CONTENT))
//                .andExpect(jsonPath("$.totalWords").value(LessonFactory.DEFAULT_TOTAL_WORDS));
//    }
//
//    @Test
//    void getContent_shouldReturn404_whenLessonDoesNotExist() throws Exception {
//
//
//
//        when(lessonService.getLessonContent(lessonId, userId))
//                .thenThrow(new ResourceNotFoundException(ErrorMessages.LESSON_NOT_FOUND));
//
//        performGetLessonContent(lessonId, userId)
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.message").value(ErrorMessages.LESSON_NOT_FOUND));
//    }
//
//    @Test
//    void getContent_shouldReturn403_whenUserDoesNotOwnLesson() throws Exception {
//
//
//
//        when(lessonService.getLessonContent(lessonId, userId))
//                .thenThrow(new ForbiddenAccessException(ErrorMessages.ACCESS_DENIED));
//
//        performGetLessonContent(lessonId, userId)
//                .andExpect(status().isForbidden())
//                .andExpect(jsonPath("$.message").value(ErrorMessages.ACCESS_DENIED));
//    }

    @Test
    void getContent_shouldReturn400_whenUserIdHeaderMissing() throws Exception {
        mockMvc.perform(get("/content/{id}/content", lessonId))
                .andExpect(status().isBadRequest());
    }
}
