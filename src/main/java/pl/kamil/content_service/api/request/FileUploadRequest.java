package pl.kamil.content_service.api.request;

import org.springframework.web.multipart.MultipartFile;
import pl.kamil.content_service.shared.validation.annotations.AllowedContentTypes;
import pl.kamil.content_service.shared.validation.annotations.MaxFileSize;
import pl.kamil.content_service.shared.validation.annotations.NotEmptyFile;

public record FileUploadRequest(
        @NotEmptyFile
        @MaxFileSize(1_048_576) // 1MB
        @AllowedContentTypes({"text/plain"})
        MultipartFile file) {
}
