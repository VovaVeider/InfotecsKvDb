package org.vladimir.infotecs.keyvaluedb.controller;


import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.vladimir.infotecs.keyvaluedb.dto.DeleteValueByKeyResponse;
import org.vladimir.infotecs.keyvaluedb.dto.GetValueByKeyResponse;
import org.vladimir.infotecs.keyvaluedb.dto.LoadDumpRequest;
import org.vladimir.infotecs.keyvaluedb.dto.SetValueByKeyRequest;
import org.vladimir.infotecs.keyvaluedb.exception.KeyNotFound;
import org.vladimir.infotecs.keyvaluedb.model.ValueWithExpirationTime;
import org.vladimir.infotecs.keyvaluedb.service.KeyValueDbService;

import java.util.Map;

@RestController

public class KeyValueDbControllerImpl implements KeyValueDbController {

    private final KeyValueDbService keyValueDbService;

    KeyValueDbControllerImpl(@Autowired KeyValueDbService keyValueDbService) {
        this.keyValueDbService = keyValueDbService;
    }


    public ResponseEntity<GetValueByKeyResponse> getValueByKey(@PathVariable String key) {
        String value = keyValueDbService.getValueByKey(key).orElseThrow(KeyNotFound::new);
        return ResponseEntity.ok(new GetValueByKeyResponse(value));
    }


    public ResponseEntity<Void> setValueByKey(@PathVariable String key, @Valid @RequestBody SetValueByKeyRequest setValueByKeyRequest) {
        Long ttl = setValueByKeyRequest.getTtl();
        String value = setValueByKeyRequest.getValue();
        keyValueDbService.setValueByKey(key, value, ttl);
        return ResponseEntity.ok().build();
    }


    public ResponseEntity<DeleteValueByKeyResponse> deleteValueByKey(@PathVariable String key) {
        DeleteValueByKeyResponse response = new DeleteValueByKeyResponse();
        response.setValue(
                keyValueDbService
                        .deleteValueByKey(key)
                        .orElseThrow(KeyNotFound::new));
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, ValueWithExpirationTime>> getDump() {
        return ResponseEntity.ok(keyValueDbService.getDump());
    }


    public ResponseEntity<Void> restoreFromDump(@Valid @RequestBody LoadDumpRequest requestBody) {
        keyValueDbService.restoreFromDump(requestBody.getDump());
        return ResponseEntity.ok().build();
    }

}
