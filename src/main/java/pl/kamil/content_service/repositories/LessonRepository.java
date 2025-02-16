package pl.kamil.content_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.kamil.content_service.models.Lesson;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

}
