package com.mycompany.testtask.rcpclient.exception;

/**
 * Exception for remote call
 *
 * Created by Dima on 29.08.2016.
 */
public class RemoteCallException extends RuntimeException {
    public RemoteCallException(String message) {
        super(message);
    }
}
