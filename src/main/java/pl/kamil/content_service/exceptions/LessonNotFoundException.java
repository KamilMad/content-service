package pl.kamil.content_service.exceptions;

public class LessonNotFoundException extends RuntimeException{

    public LessonNotFoundException(String message) {
        super(message);
    }
}
