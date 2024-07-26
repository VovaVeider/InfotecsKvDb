package org.vladimir.infotecs.keyvaluedb.controller;


import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vladimir.infotecs.keyvaluedb.dto.DeleteValueByKeyResponse;
import org.vladimir.infotecs.keyvaluedb.dto.GetValueByKeyResponse;
import org.vladimir.infotecs.keyvaluedb.dto.LoadDumpRequest;
import org.vladimir.infotecs.keyvaluedb.dto.SetValueByKeyRequest;
import org.vladimir.infotecs.keyvaluedb.exception.KeyNotFound;
import org.vladimir.infotecs.keyvaluedb.service.KeyValueDbService;

import java.util.Map;

@RestController

public class KeyValueDbControllerImpl implements KeyValueDbController{

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


    public ResponseEntity<DeleteValueByKeyResponse> deleteValueByKey(@PathVariable  String key) {
        DeleteValueByKeyResponse response = new DeleteValueByKeyResponse();
        response.setValue(
                keyValueDbService
                        .deleteValueByKey(key)
                        .orElseThrow(KeyNotFound::new));
        return ResponseEntity.ok(response);
    }


    public ResponseEntity<Map<String, String>> getDump() {
        Map<String, String> dump = keyValueDbService.getAllValues();
        return ResponseEntity.ok(dump);
    }


    public ResponseEntity<Void> loadDump(@Valid @RequestBody LoadDumpRequest requestBody) {
        keyValueDbService.loadAllValuesByKey(requestBody.getDump());
        return ResponseEntity.ok().build();
    }

}
