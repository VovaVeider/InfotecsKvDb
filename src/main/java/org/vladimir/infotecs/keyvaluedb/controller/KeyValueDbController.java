package org.vladimir.infotecs.keyvaluedb.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jdk.jfr.Description;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vladimir.infotecs.keyvaluedb.dto.DeleteValueByKeyResponse;
import org.vladimir.infotecs.keyvaluedb.dto.GetValueByKeyResponse;
import org.vladimir.infotecs.keyvaluedb.dto.LoadDumpRequest;
import org.vladimir.infotecs.keyvaluedb.dto.SetValueByKeyRequest;
import org.vladimir.infotecs.keyvaluedb.exception.KeyNotFound;
import org.vladimir.infotecs.keyvaluedb.model.ValueWithExpirationTime;

import java.util.Map;

@Tag(name = "Key value storage")
@Description("Simple KV db. Time format is unix time ")
@RequestMapping("/api")
public interface KeyValueDbController {

    @Operation(summary = "Get value by key", description = "Retrieves the value associated with the specified key")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved value",
                    content = @Content(schema = @Schema(implementation = GetValueByKeyResponse.class))),
            @ApiResponse(responseCode = "404", description = "Key not found",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("keys/{key}")
    ResponseEntity<GetValueByKeyResponse> getValueByKey(@PathVariable String key) throws KeyNotFound;

    @Operation(summary = "Set value by key", description = "Sets the value for the specified key. Ttl must be positive or zero (use default server ttl)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully set value")
    })
    @PostMapping("keys/{key}")
    ResponseEntity<Void> setValueByKey(@PathVariable String key,
                                       @Valid @RequestBody(description = "Request body containing the value and ttl",
                                               required = true,
                                               content = @Content(schema = @Schema(implementation = SetValueByKeyRequest.class)))
                                       SetValueByKeyRequest setValueByKeyRequest);

    @Operation(summary = "Delete value by key", description = "Deletes the value associated with the specified key")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted value",
                    content = @Content(schema = @Schema(implementation = DeleteValueByKeyResponse.class))),
            @ApiResponse(responseCode = "404", description = "Key not found",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    @DeleteMapping("keys/{key}")
    ResponseEntity<DeleteValueByKeyResponse> deleteValueByKey(@PathVariable String key) throws KeyNotFound;

    @Operation(summary = "Get dump", description = "Retrieves storage dump")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved dump",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @GetMapping("dump")
    ResponseEntity<Map<String, ValueWithExpirationTime>> getDump();

    @Operation(summary = "Load dump", description = "Restore storage state  from the provided dump")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully loaded dump")
    })
    @PostMapping("dump")
    ResponseEntity<Void> restoreFromDump(@Valid @RequestBody(description = "Request body containing the dump, " +
            "expiration time in unix time (seconds from epoch utc)",
            required = true,
            content = @Content(schema = @Schema(implementation = LoadDumpRequest.class)))
                                         LoadDumpRequest requestBody);
}

