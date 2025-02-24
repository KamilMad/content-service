package pl.kamil.content_service.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kamil.content_service.dtos.LessonRequest;
import pl.kamil.content_service.models.Lesson;
import pl.kamil.content_service.repositories.LessonRepository;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;

    public Lesson createLesson(LessonRequest lessonRequest) {

        Lesson lesson = new Lesson();
        lesson.setTitle(lessonRequest.title());
        lesson.setContent(lessonRequest.content());

        return lessonRepository.save(lesson);
    }
}
