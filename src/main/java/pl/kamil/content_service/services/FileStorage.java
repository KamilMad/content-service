package pl.kamil.content_service.services;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import pl.kamil.content_service.dtos.FileUploadResponse;

public interface FileStorage {

    public FileUploadResponse storeFile(MultipartFile file);
    public void deleteFile(String key);
    public String getFileContent(String fileKey);
}
