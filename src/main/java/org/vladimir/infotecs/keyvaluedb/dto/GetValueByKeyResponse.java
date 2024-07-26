package org.vladimir.infotecs.keyvaluedb.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetValueByKeyResponse {
    @NotNull
    private String value;
}