package org.vladimir.infotecs.keyvaluedb.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.vladimir.infotecs.keyvaluedb.model.ValueWithExpirationTime;
import org.vladimir.infotecs.keyvaluedb.validation.NotNullKeyAndValueInMap;

import java.util.Map;

@Data
public class LoadDumpRequest {
    @NotNull(message = "dump object must be presented")
    @NotNullKeyAndValueInMap(message = "Dump object must not have null keys or values")
    private Map<String, ValueWithExpirationTime> dump;
}

