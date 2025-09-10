package br.com.stefanini.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class DynamoDbErroException extends RuntimeException {
    private final HttpStatusCode status;
    private final Object msg;

    public DynamoDbErroException(HttpStatus status, Object msg) {
        super();
        this.status = status;
        this.msg = msg;
    }

    public HttpStatusCode getStatus() {
        return status;
    }

    public Object getMsg() {
        return msg;
    }
}
