package org.vladimir.infotecs.keyvaluedb.model;


import lombok.Data;
import lombok.NonNull;

import java.time.LocalDateTime;

@Data
public class ValueWithExpirationTime {
    @NonNull
    String value;
    @NonNull
    LocalDateTime expirationTime;
}