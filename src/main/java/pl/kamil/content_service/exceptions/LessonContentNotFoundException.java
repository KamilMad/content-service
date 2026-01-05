package pl.kamil.content_service.exceptions;

public class LessonContentNotFoundException extends RuntimeException {
    public LessonContentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public LessonContentNotFoundException(String message) {
        super(message);
    }
}
