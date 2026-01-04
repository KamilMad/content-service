package pl.kamil.content_service.dtos;

import org.springframework.web.multipart.MultipartFile;
import pl.kamil.content_service.validation.annotations.AllowedContentTypes;
import pl.kamil.content_service.validation.annotations.MaxFileSize;
import pl.kamil.content_service.validation.annotations.NotEmptyFile;

public record FileUploadRequest(
        @NotEmptyFile
        @MaxFileSize(1_048_576) // 1MB
        @AllowedContentTypes({"/text/plain"})
        MultipartFile file) {
}
