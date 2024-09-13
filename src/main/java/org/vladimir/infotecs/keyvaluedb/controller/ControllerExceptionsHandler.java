package org.vladimir.infotecs.keyvaluedb.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.vladimir.infotecs.keyvaluedb.dto.ErrorResponse;
import org.vladimir.infotecs.keyvaluedb.exception.KeyNotFound;

import java.util.List;

@RestControllerAdvice
public class ControllerExceptionsHandler {

    private static final Logger logger = LoggerFactory.getLogger(ControllerExceptionsHandler.class);

    @ExceptionHandler(KeyNotFound.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleKeyNotFoundException(KeyNotFound ex, WebRequest request) {
        logger.error("KeyNotFound Exception: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse("Key not found");
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception, WebRequest request) {
        List<ObjectError> allErrors = exception.getBindingResult().getAllErrors();
        StringBuilder errorMessage = new StringBuilder();

        for (ObjectError error : allErrors) {
            errorMessage.append(error.getDefaultMessage()).append(";");
        }

        logger.error("MethodArgumentNotValidException: {}", errorMessage.toString());
        return new ResponseEntity<>(new ErrorResponse(errorMessage.toString()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        String firstErrorMessage = ex.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("Unknown constraint violation error");

        logger.error("ConstraintViolationException: {}", firstErrorMessage);
        ErrorResponse errorResponse = new ErrorResponse(firstErrorMessage);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException ex, WebRequest request) {
        logger.error("NoResourceFoundException: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse("Such endpoint not exists");
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        logger.error("Internal server error: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse("Internal server error");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}