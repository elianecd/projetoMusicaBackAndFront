package com.example.projetomusica.rest;

import com.example.projetomusica.exceptions.BandaAlreadyExistsException;
import com.example.projetomusica.exceptions.UserAlreadyExistsException;
import com.example.projetomusica.rest.exception.ApiErrors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApplicationControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Object handleValidationErrors(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        List<String> messages = bindingResult.getAllErrors()
                .stream()
                .map(objectError -> objectError.getDefaultMessage())
                .collect(Collectors.toList());
        return new ApiErrors(messages);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrors> handleResponseStatusException(ResponseStatusException ex) {
        String messageError = ex.getReason();
        HttpStatus codigoStatus = (HttpStatus) ex.getStatusCode();
        ApiErrors apiErrors = new ApiErrors(messageError);
        return new ResponseEntity(apiErrors, codigoStatus);
    }

    @ExceptionHandler(BandaAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiErrors> handleBandaAlreadyExistsException(BandaAlreadyExistsException ex) {
        ApiErrors apiErrors = new ApiErrors(ex.getMessage());
        return new ResponseEntity<>(apiErrors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiErrors> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        ApiErrors apiErrors = new ApiErrors(ex.getMessage());
        return new ResponseEntity<>(apiErrors, HttpStatus.BAD_REQUEST);
    }
}
