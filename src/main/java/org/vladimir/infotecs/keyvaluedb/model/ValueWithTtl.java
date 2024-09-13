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

    @NotNull(message = "ttl must be presented use zero to set default server ttl value")
    @PositiveOrZero(message = "ttl must be zero(use default server ttl value) or positive integer(long)")
    Long ttl;

}
