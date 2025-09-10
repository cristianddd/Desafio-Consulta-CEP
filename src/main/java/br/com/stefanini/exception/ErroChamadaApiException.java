package br.com.stefanini.exception;

import org.springframework.http.HttpStatus;

public class ErroChamadaApiException extends Throwable {
    private HttpStatus status;
    private Object msg;

    public ErroChamadaApiException(HttpStatus httpStatus, Object msg) {
        super();
        this.status = httpStatus;
        this.msg = msg;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Object getMsg() {
        return msg;
    }
}
