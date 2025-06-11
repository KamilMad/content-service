package pl.kamil.content_service.dtos;

public record FileUploadResponse(
        Long id,
        String originalName,
        String contentType,
        long size,
        String preSignedUrl,
        String objectKey
) { }
