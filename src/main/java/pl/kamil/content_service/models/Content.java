package pl.kamil.content_service.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String s3Key; // path to file in s3
    private long total_words;

    @CreationTimestamp
    private Instant created_at;

    @OneToOne
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

}
