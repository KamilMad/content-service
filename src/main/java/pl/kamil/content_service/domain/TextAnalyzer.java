package pl.kamil.content_service.domain;

import org.springframework.web.multipart.MultipartFile;
import pl.kamil.content_service.shared.ErrorMessages;
import pl.kamil.content_service.application.exception.FileProcessingException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TextAnalyzer {

    public static long countWordsInFile(MultipartFile multipartFile) {

        String content = decodeFile(multipartFile);

        String[] words = content.trim().split("\\s+");

        return words.length;
    }

    private static String decodeFile(MultipartFile multipartFile) {
        try {
            return new String(multipartFile.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new FileProcessingException(ErrorMessages.FILE_DECODE_FAILED, e);
        }
    }

}
