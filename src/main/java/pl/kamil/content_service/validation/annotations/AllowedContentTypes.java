package pl.kamil.content_service.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import pl.kamil.content_service.validation.validators.ContentTypeValidator;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ContentTypeValidator.class)
@Documented
public @interface AllowedContentTypes {

    String[] value(); //allowed types
    String message() default "Invalid content type";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
