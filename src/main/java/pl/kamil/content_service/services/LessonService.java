package pl.kamil.content_service.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.kamil.content_service.dtos.LessonRequest;
import pl.kamil.content_service.exceptions.LessonNotFoundException;
import pl.kamil.content_service.models.Lesson;
import pl.kamil.content_service.repositories.LessonRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

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

    public List<Lesson> findAll() {
        return lessonRepository.findAll();
    }

    public void saveLessonFromFile(MultipartFile file) throws IOException {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        Lesson lesson = new Lesson();
        lesson.setTitle(file.getOriginalFilename());
        lesson.setContent(content);

        lessonRepository.save(lesson);
    }

    public Lesson findById(Long id) {
        Optional<Lesson> optionalLesson = lessonRepository.findById(id);

        if (optionalLesson.isPresent()) {
            return optionalLesson.get();
        }else {
            //throw new RuntimeException("Lesson not found");
            throw new LessonNotFoundException("Lesson with id: " + id + " not found");
        }
    }
}
