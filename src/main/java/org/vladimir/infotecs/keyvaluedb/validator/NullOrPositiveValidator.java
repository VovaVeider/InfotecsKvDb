package org.vladimir.infotecs.keyvaluedb.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.vladimir.infotecs.keyvaluedb.validation.NullOrPositive;

public class NullOrPositiveValidator implements ConstraintValidator<NullOrPositive, Long> {
    @Override
    public void initialize(NullOrPositive constraintAnnotation) {
    }

    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {
        return value == null || value > 0;
    }
}
