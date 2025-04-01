package pl.kamil.content_service.models;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;

@Entity
@Table(name = "lessons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private long total_words;

    private Instant created_at;
    private Instant updated_at;

    private Long createdBy;
    private String fileUrl;

    public static Lesson create(String title, Long userId, long totalWords) {
        Lesson lesson = new Lesson();
        lesson.setTitle(title);
        lesson.setCreatedBy(userId);
        lesson.setTotal_words(totalWords);
        lesson.setCreated_at(Instant.now());
        lesson.setUpdated_at(Instant.now());
        return lesson;
    }
}
