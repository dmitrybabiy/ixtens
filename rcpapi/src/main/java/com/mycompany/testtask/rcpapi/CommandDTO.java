package com.mycompany.testtask.rcpapi;

import java.io.Serializable;
import java.util.Arrays;

/**
 * DTO object for rcp request command
 *
 * Created by Dima on 26.08.2016.
 */
public class CommandDTO implements Serializable {
    private long id;
    private String serviceName;
    private String methodName;
    private Object[] params;

    public CommandDTO(long id, String serviceName, String methodName, Object[] params) {
        this.id = id;
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.params = params;
    }

    public long getId() {
        return id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public Object[] getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "CommandDTO{" +
                "id=" + id +
                ", serviceName='" + serviceName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", params=" + Arrays.toString(params) +
                '}';
    }
}
