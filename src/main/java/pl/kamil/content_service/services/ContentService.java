package pl.kamil.content_service.services;

import org.springframework.stereotype.Service;
import pl.kamil.content_service.models.Content;

@Service
public class ContentService {

    public Content createContent(String s3key, long totalWords) {
        return Content.builder()
                .s3Key(s3key)
                .totalWords(totalWords)
                .build();
    }
}
