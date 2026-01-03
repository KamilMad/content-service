package pl.kamil.content_service.util;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MaxFileSizeValidator.class)
@Documented
public @interface MaxFileSize {

    long value(); //max size in bytes

    String message() default "File size exceeds the maximum allowed size";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
