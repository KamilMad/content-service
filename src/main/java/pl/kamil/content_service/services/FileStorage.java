package pl.kamil.content_service.services;

import org.springframework.web.multipart.MultipartFile;
import pl.kamil.content_service.api.response.FileUploadResponse;

public interface FileStorage {

    FileUploadResponse storeFile(MultipartFile file);
    void deleteFile(String key);
    String getFileContent(String fileKey);
}
