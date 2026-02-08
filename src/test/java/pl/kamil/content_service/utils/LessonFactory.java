package pl.kamil.content_service.utils;

import org.springframework.mock.web.MockMultipartFile;
import pl.kamil.content_service.dtos.LessonContentResponse;
import pl.kamil.content_service.dtos.LessonResponse;
import pl.kamil.content_service.models.Content;
import pl.kamil.content_service.models.Lesson;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

public class LessonFactory {

    // CONSTANT
    public static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final UUID OTHER_TEST_USER_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");
    public static final UUID TEST_LESSON_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    public static final String DEFAULT_TITLE = "Default lesson title";

    public static final UUID TEST_CONTENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    public static final String DEFAULT_FILE_CONTENT = "Default file content";
    public static final Long DEFAULT_TOTAL_WORDS = 100L;
    public static final String DEFAULT_S3_KEY = "s3/path";

    // FILE HELPERS
    public static final String DEFAULT_NAME = "file";
    public static final String DEFAULT_ORIGINAL_FILENAME = "filename.txt";
    public static final String DEFAULT_CONTENT_TYPE = "text/plain";
    public static final byte[] DEFAULT_CONTENT = "Sample content".getBytes();


    public static MockMultipartFile createMockFile() {
        return new MockMultipartFile(
                DEFAULT_NAME,
                DEFAULT_ORIGINAL_FILENAME,
                DEFAULT_CONTENT_TYPE,
                DEFAULT_CONTENT);
    }

    public static MockMultipartFile createCustomMockFile(String filename, String contentType, byte[] content) {
        return new MockMultipartFile(
                DEFAULT_NAME,
                filename != null ? filename : DEFAULT_ORIGINAL_FILENAME,
                contentType != null ? contentType : DEFAULT_CONTENT_TYPE,
                content != null ? content : DEFAULT_CONTENT
        );
    }

    // DTO HELPERS
    public static LessonResponse createLessonResponse() {
        return LessonResponse.builder()
                .id(TEST_LESSON_ID)
                .title(DEFAULT_ORIGINAL_FILENAME)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // CONTENT HELPER
    public static Content createContent() {
        return Content.builder()
                .id(TEST_CONTENT_ID)
                .s3Key(DEFAULT_S3_KEY)
                .totalWords(DEFAULT_TOTAL_WORDS)
                .createdAt(Instant.now())
                .build();
    }

    public static LessonContentResponse createLessonContentResponse() {
        return LessonContentResponse.builder()
                .fileText(DEFAULT_FILE_CONTENT)
                .totalWords(DEFAULT_TOTAL_WORDS)
                .build();
    }

    // LESSON HELPER
    public static Lesson createLesson() {
        return Lesson.builder()
                .id(TEST_LESSON_ID)
                .createdBy(TEST_USER_ID)
                .title(DEFAULT_ORIGINAL_FILENAME)
                .content(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static List<Lesson> createLessonList(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> createLesson().toBuilder()
                        .id(UUID.randomUUID())
                        .title(DEFAULT_ORIGINAL_FILENAME + " " + i)
                                .build()
                        ).toList();
    }

    public static Lesson createLessonWithContent() {
        return createLesson().toBuilder()
                .content(createContent())
                .build();
    }

}
