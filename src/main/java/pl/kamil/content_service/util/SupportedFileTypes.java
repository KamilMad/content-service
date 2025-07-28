package pl.kamil.content_service.util;

import org.springframework.http.MediaType;

import java.util.List;

public class SupportedFileTypes {

    public static final List<String> SUPPORTED_TYPES = List.of(
            MediaType.TEXT_PLAIN_VALUE
    );
}
