package pl.kamil.content_service.application.exception;

public class FileProcessingException extends RuntimeException{
    public FileProcessingException(String message) {
        super(message);
    }

    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
