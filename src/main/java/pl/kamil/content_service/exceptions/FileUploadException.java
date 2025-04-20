package pl.kamil.content_service.exceptions;

public class FileUploadException extends RuntimeException{

    public FileUploadException(String message) {
        super(message);
    }
}
