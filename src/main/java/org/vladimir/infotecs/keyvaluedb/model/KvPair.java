package org.vladimir.infotecs.keyvaluedb.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KvPair {
    private String key;
    private ValueWithExpirationTime value;
}