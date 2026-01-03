package pl.kamil.content_service.util;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotEmptyMultipartFileValidator.class)
public @interface NotEmptyFile {

    String message() default "File must not be empty";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
