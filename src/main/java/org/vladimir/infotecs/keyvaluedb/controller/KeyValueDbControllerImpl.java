package org.vladimir.infotecs.keyvaluedb.controller;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vladimir.infotecs.keyvaluedb.dto.DeleteValueByKeyResponse;
import org.vladimir.infotecs.keyvaluedb.dto.GetValueByKeyResponse;
import org.vladimir.infotecs.keyvaluedb.dto.LoadDumpRequest;
import org.vladimir.infotecs.keyvaluedb.dto.SetValueByKeyRequest;
import org.vladimir.infotecs.keyvaluedb.exception.KeyNotFound;
import org.vladimir.infotecs.keyvaluedb.model.ValueWithExpirationTime;
import org.vladimir.infotecs.keyvaluedb.service.KeyValueService;

import java.util.Arrays;
import java.util.Map;

@RestController
public class KeyValueDbControllerImpl implements KeyValueDbController {

    private final KeyValueService keyValueService;

    KeyValueDbControllerImpl(@Autowired KeyValueService keyValueService) {
        this.keyValueService = keyValueService;
    }


    public ResponseEntity<GetValueByKeyResponse> getValueByKey(@PathVariable String key) {
        String value = keyValueService.getValueByKey(key).orElseThrow(KeyNotFound::new);
        return ResponseEntity.ok(new GetValueByKeyResponse(value));
    }


    public ResponseEntity<Void> setValueByKey(@PathVariable String key, @Valid @RequestBody SetValueByKeyRequest setValueByKeyRequest) {
        Long ttl = setValueByKeyRequest.getTtl();
        String value = setValueByKeyRequest.getValue();
        keyValueService.setValueByKey(key, value, ttl);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/test")
    private ResponseEntity<String> test(HttpServletRequest request, HttpServletResponse response) {
        String cookieName = "myCookie";
        String cookieValue = "cookieValue";
        boolean cookieExists = false;

        // Получаем все куки из запроса
        Cookie[] cookies = request.getCookies();
        System.out.println("==============================================================");
        if (cookies != null) {
            // Проверяем, существует ли кука
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    cookieExists = true;
                    System.out.println(cookie.getName()+":"+cookie.getValue());
                    break;
                }
            }
        }

        if (cookieExists) {
            System.out.println("Кука есть"+cookieName +" "+cookieValue);
            return new ResponseEntity("Cookie '" + cookieName + "' already exists.", HttpStatusCode.valueOf(200));
        } else {
            // Если куки нет, создаем её
            Cookie newCookie = new Cookie(cookieName, cookieValue);
            newCookie.setMaxAge(7 * 24 * 60 * 60); // 7 дней
            response.addCookie(newCookie);
            System.out.println("Установка куки");
            return new ResponseEntity("Cookie '" + cookieName + "' has been added.", HttpStatusCode.valueOf(200));
        }
    }


    public ResponseEntity<DeleteValueByKeyResponse> deleteValueByKey(@PathVariable String key) {
        DeleteValueByKeyResponse response = new DeleteValueByKeyResponse();
        response.setValue(
                keyValueService
                        .deleteValueByKey(key)
                        .orElseThrow(KeyNotFound::new));
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, ValueWithExpirationTime>> getDump() {
        return ResponseEntity.ok(keyValueService.getDump());
    }


    public ResponseEntity<Void> restoreFromDump(@Valid @RequestBody LoadDumpRequest requestBody) {
        keyValueService.restoreFromDump(requestBody.getDump());
        return ResponseEntity.ok().build();
    }

}
