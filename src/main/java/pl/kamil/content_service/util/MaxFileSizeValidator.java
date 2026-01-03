package pl.kamil.content_service.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class MaxFileSizeValidator implements ConstraintValidator<MaxFileSize, MultipartFile> {

    private long maxSize;

    @Override
    public void initialize(MaxFileSize constraintAnnotation) {
      this.maxSize = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext constraintValidatorContext) {
        if (file == null)
            return true;

        return file.getSize() <= maxSize;
    }
}
