package org.vladimir.infotecs.keyvaluedb.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class SetValueByKeyRequest {
    @NotNull(message = "value must not be blank")
    private String value;

    @PositiveOrZero(message = "ttl must be zero(use default server ttl value) or positive integer(long)")
    private long ttl;
}
