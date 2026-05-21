package pl.kamil.content_service.application;

import pl.kamil.content_service.api.response.PagedResponse;

import java.util.List;

public class TextPaginator {
    public static PagedResponse<String> paginate(String fileText, int pageNo, int pageSize) {

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
