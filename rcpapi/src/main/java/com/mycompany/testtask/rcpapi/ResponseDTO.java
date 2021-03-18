package com.mycompany.testtask.rcpapi;

import java.io.Serializable;

/**
 * DTO object for rcp response
 *
 * Created by Dima on 27.08.2016.
 */
public class ResponseDTO implements Serializable {
    private long id;
    private Object result;
    private Boolean isVoid;
    private String errorMessage;

    public ResponseDTO(long id, Object result, Boolean isVoid, String errorMessage) {
        this.id = id;
        this.result = result;
        this.isVoid = isVoid;
        this.errorMessage = errorMessage;
    }

    public long getId() {
        return id;
    }

    public Object getResult() {
        return result;
    }

    public Boolean isVoid() {
        return isVoid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return "ResponseDTO{" +
                "id=" + id +
                ", result=" + result +
                ", isVoid=" + isVoid +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
