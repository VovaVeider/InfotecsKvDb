package org.vladimir.infotecs.keyvaluedb.model;


import lombok.Data;
import lombok.NonNull;

@Data
public class ValueWithExpirationTime {
    @NonNull
    String value;
    @NonNull
    Long expirationTime;
}