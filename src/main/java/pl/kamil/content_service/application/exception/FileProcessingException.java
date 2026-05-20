package pl.kamil.content_service.infrastructure.excaption;

public class FileProcessingException extends RuntimeException{
    public FileProcessingException(String message) {
        super(message);
    }

    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
