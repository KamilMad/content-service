package pl.kamil.content_service.services;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class TextAnalysisService {

    public long countWordsInFile(MultipartFile multipartFile) {

        String content = decodeFile(multipartFile);

        String[] words = content.trim().split("\\s+");

        return words.length;
    }

    private String decodeFile(MultipartFile multipartFile) {
        try {
            return new String(multipartFile.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to decode file", e);
        }
    }

}
