package pl.kamil.content_service.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;
import pl.kamil.content_service.validation.annotations.AllowedContentTypes;

import java.util.Set;

public class ContentTypeValidator implements ConstraintValidator<AllowedContentTypes, MultipartFile> {

    Set<String> allowedTypes;

    @Override
    public void initialize(AllowedContentTypes constraintAnnotation) {
        this.allowedTypes = Set.of(constraintAnnotation.value());
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext constraintValidatorContext) {
        if (file == null || file.isEmpty()) return true;
        return allowedTypes.contains(file.getContentType());
    }
}
