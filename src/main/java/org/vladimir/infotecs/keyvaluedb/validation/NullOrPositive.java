package org.vladimir.infotecs.keyvaluedb.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.vladimir.infotecs.keyvaluedb.validator.NullOrPositiveValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = NullOrPositiveValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface NullOrPositive {
    String message() default "must be null or a positive number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
