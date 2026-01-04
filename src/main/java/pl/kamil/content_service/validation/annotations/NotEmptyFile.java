package pl.kamil.content_service.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import pl.kamil.content_service.validation.validators.NotEmptyFileValidator;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotEmptyFileValidator.class)
@Documented
public @interface NotEmptyFile {

    String message() default "File must not be empty";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
