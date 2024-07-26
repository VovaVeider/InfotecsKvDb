package org.vladimir.infotecs.keyvaluedb.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.vladimir.infotecs.keyvaluedb.validation.NotNullKeyAndValueInMap;

import java.util.Map;

public class NotNullKeyAndValueInMapValidator implements ConstraintValidator<NotNullKeyAndValueInMap, Map<?, ?>> {

@Override
public void initialize(NotNullKeyAndValueInMap constraintAnnotation) {
}

@Override
public boolean isValid(Map<?, ?> map, ConstraintValidatorContext context) {
    if (map == null) {
        return true;
    }

    for (Map.Entry<?, ?> entry : map.entrySet()) {
        if (entry.getKey() == null || entry.getValue() == null) {
            return false;
        }
    }
    return true;
}
}
