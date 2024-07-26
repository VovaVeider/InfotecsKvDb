package org.vladimir.infotecs.keyvaluedb.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
@AllArgsConstructor
@Data
public class ValueWithTtl {
    @NotNull(message = "value must not be presented")
    String value;
    @PositiveOrZero(message = "ttl must be null or a positive number")
    Long ttl;
}
