package br.com.stefanini.consultacep.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class CepNotFoundException extends RuntimeException {

    private final HttpStatusCode status;
    private final String msg;

    public CepNotFoundException(HttpStatusCode status, String msg) {
        super();
        this.status = status;
        this.msg = msg;
    }

    public HttpStatusCode getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }
}
