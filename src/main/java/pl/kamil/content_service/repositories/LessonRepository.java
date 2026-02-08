package pl.kamil.content_service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.kamil.content_service.models.Lesson;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    Page<Lesson> findAllByCreatedBy(UUID userId, Pageable pageable);
    Optional<Lesson> findByIdAndCreatedBy(UUID lessonId, UUID userId);

}
