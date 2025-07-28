package pl.kamil.content_service.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pl.kamil.content_service.exceptions.FileProcessingException;

@Component
public class FileValidator {

    private static final long MAX_FILE_SIZE = 1 * 1024 * 1024; // 1MB

    public void validate(MultipartFile file) {
        String contentType = file.getContentType();
        if(!SupportedFileTypes.SUPPORTED_TYPES.contains(contentType)) {
            throw new FileProcessingException("Unsupported file type: " + contentType);
        }

        if(file.getSize() > MAX_FILE_SIZE) {
            throw new FileProcessingException("File exceeds the maximum size :" + MAX_FILE_SIZE);
        }
    }
}
