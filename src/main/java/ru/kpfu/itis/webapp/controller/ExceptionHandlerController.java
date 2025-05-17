package ru.kpfu.itis.webapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.kpfu.itis.webapp.exceptions.DataNotFoundException;
import ru.kpfu.itis.webapp.exceptions.ServiceException;

@ControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler({DataNotFoundException.class})
    public ResponseEntity<String> handleNotFound(Exception ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler({ServiceException.class})
    public ResponseEntity<String> handleBadRequest(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

}
