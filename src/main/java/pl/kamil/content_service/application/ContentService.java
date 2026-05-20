package pl.kamil.content_service.application;

import org.springframework.stereotype.Service;
import pl.kamil.content_service.api.response.PagedResponse;
import pl.kamil.content_service.domain.Content;
import pl.kamil.content_service.infrastructure.ContentRepository;

import java.util.List;

@Service
public class ContentService {

    private final ContentRepository contentRepository;

    public ContentService(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public Content createContent(String s3key, long totalWords) {
        return Content.builder()
                .s3Key(s3key)
                .totalWords(totalWords)
                .build();
    }

    public PagedResponse<String> createContentPage(String fileText, int pageNo, int pageSize) {

        int totalLength = fileText.length();
        int totalPages = (int) Math.ceil((double) totalLength / pageSize);


        int start = Math.min(pageNo * pageSize, totalLength);
        int end = Math.min(start + pageSize, totalLength);

        // round to the nearest space to not split words in half
        if (end < totalLength) {
            int nextSpace = fileText.indexOf(" ", end);
            if (nextSpace != -1 && nextSpace < end + 20) {
                end = nextSpace;
            }
        }

        String chunk = fileText.substring(start, end).trim();
        boolean hasNext = end < totalLength;

        return new PagedResponse<>(
                List.of(chunk),
                pageNo,
                pageSize,
                totalLength,
                totalPages,
                !hasNext);
    }
}
