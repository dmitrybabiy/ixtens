package com.mycompany.testtask.rcpserver.exception;

/**
 * Class exception for no such service case
 *
 * Created by Dima on 28.08.2016.
 */
public class NoSuchService extends RuntimeException {
    public NoSuchService(String message) {
        super(message);
    }
}
