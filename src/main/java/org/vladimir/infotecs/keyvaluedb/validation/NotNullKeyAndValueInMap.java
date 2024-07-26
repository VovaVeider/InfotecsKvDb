package org.vladimir.infotecs.keyvaluedb.validation;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.vladimir.infotecs.keyvaluedb.validator.NotNullKeyAndValueInMapValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = NotNullKeyAndValueInMapValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface NotNullKeyAndValueInMap {
    String message() default "Map must not have null keys or values";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

