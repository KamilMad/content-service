package pl.kamil.content_service.domain;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
public class Content {

    protected Content() {
    }

    private Content(String s3Key, long totalWords) {
        if (s3Key == null || s3Key.isBlank()) {
            throw new IllegalArgumentException("S3 key cannot be null or blank");
        }
        if (totalWords < 0) {
            throw new IllegalArgumentException("Total words cannot be negative");
        }
        this.s3Key = s3Key;
        this.totalWords = totalWords;
        this.createdAt = Instant.now();
    }

    public static Content create(String s3Key, long totalWords) {
        return new Content(s3Key, totalWords);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String s3Key; // path to file in s3
    private long totalWords;

    @CreationTimestamp
    private Instant createdAt;

    @OneToOne(
            fetch = FetchType.LAZY,
            optional = false)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

}
