package org.vladimir.infotecs.keyvaluedb.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.vladimir.infotecs.keyvaluedb.validation.NotNullKeyAndValueInMap;

import java.util.Map;

@Data
public class GetDumpResponse {
    @NotNull(message = "dump must be presented")
    @NotNullKeyAndValueInMap(message = "Dumb object must not have null keys or values")
    private Map<String, String> dump;
}

