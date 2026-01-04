package pl.kamil.content_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.kamil.content_service.models.Lesson;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    List<Lesson> findAllByCreatedBy(UUID userId);
    Optional<Lesson> findByIdAndCreatedBy(UUID lessonId, UUID userId);

}
