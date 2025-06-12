package pl.kamil.content_service.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

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
    private String s3Key;

    public static Lesson create(String title, Long userId, long total_words) {
        Lesson lesson = new Lesson();
        lesson.setTitle(title);
        lesson.setTotal_words(total_words);
        lesson.setCreatedBy(userId);
        lesson.setCreated_at(Instant.now());
        lesson.setUpdated_at(Instant.now());
        return lesson;
    }
}
