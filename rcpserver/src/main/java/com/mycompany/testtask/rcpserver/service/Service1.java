package com.mycompany.testtask.rcpserver.service;

import java.util.Date;

/**
 * Target service for remote calling
 * <p>
 * Created by Dima on 26.08.2016.
 */
public class Service1 {
    public void sleep(Long millis) throws InterruptedException {
        Thread.sleep(millis.longValue());
    }

    public Date getCurrentDate() {
        return new Date();
    }

    public Long sum(Long a, Long b) {
        return a + b;
    }

    public void exception() {
        Integer.parseInt("abc");
    }
}
