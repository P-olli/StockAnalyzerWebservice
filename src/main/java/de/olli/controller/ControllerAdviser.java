package de.olli.controller;

import com.mongodb.MongoWriteException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ControllerAdviser {

    @ExceptionHandler()
    public ResponseEntity handleMongoException(MongoWriteException ex) {
        return new ResponseEntity("Entity already stored in database", HttpStatus.CONFLICT);
    }


}
