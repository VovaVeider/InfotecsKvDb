package org.vladimir.infotecs.keyvaluedb.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.vladimir.infotecs.keyvaluedb.validation.NullOrPositive;

@Data
public class SetValueByKeyRequest {
    @NotNull(message = "value must not be blank")
    private String value;

    @NullOrPositive(message = "ttl must be null or a positive number")
    private Long ttl;
}
