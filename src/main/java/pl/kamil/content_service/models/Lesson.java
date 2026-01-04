package pl.kamil.content_service.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lessons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;
    private Long createdBy;

    private String title;
    private long total_words;
    private String s3Key;

    @OneToOne(mappedBy = "lesson", cascade = CascadeType.ALL)
    private Content content;

    @CreationTimestamp
    private Instant created_at;
    @UpdateTimestamp
    private Instant updated_at;




    public static Lesson create(String title, Long userId, long total_words) {
        Lesson lesson = new Lesson();
        lesson.setTitle(title);
        lesson.setTotal_words(total_words);
        lesson.setCreatedBy(userId);
        return lesson;
    }
}
