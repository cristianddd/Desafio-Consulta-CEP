package br.com.stefanini.consultacep.exception.response;

import br.com.stefanini.consultacep.exception.CepNotFoundException;
import br.com.stefanini.consultacep.exception.DynamoDbErroException;
import br.com.stefanini.consultacep.exception.ErroChamadaApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CepNotFoundException.class)
    public BaseExceptionResponse handleCepNotFound(CepNotFoundException ex) {
        BaseExceptionResponse body = new BaseExceptionResponse();
                body.setTimestamp(LocalDateTime.now());
                body.setStatus(ex.getStatus().value());
                body.setError(ex.getMsg().toString());

        return body;
    }
    @ExceptionHandler(DynamoDbErroException.class)
    public BaseExceptionResponse handleCepNotFound(DynamoDbErroException ex) {
        BaseExceptionResponse body = new BaseExceptionResponse();
                body.setTimestamp(LocalDateTime.now());
                body.setStatus(ex.getStatus().value());
                body.setError(ex.getMsg().toString());

        return body;
    }

    @ExceptionHandler(ErroChamadaApiException.class)
    public BaseExceptionResponse handleCepNotFound(ErroChamadaApiException ex) {
        BaseExceptionResponse body = new BaseExceptionResponse();
                body.setTimestamp(LocalDateTime.now());
                body.setStatus(ex.getStatus().value());
                body.setError(ex.getMsg().toString());
        return body;
    }
}
