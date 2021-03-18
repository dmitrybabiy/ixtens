package com.mycompany.testtask.rcpserver;

import com.mycompany.testtask.rcpserver.exception.NoSuchService;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton contains services mapping and provides calling services by service name and method name
 *
 * Created by Dima on 26.08.2016.
 */
public class RcpServicesCallerSingleton {
    private Map<String, Object> name2service;
    private static RcpServicesCallerSingleton instance = null;

    static {
        if (instance == null) {
            try {
                instance = new RcpServicesCallerSingleton();
            } catch (IOException e) {
                throw new ExceptionInInitializerError(e);
            }
        }
    }

    public static RcpServicesCallerSingleton getInstance() {
        return instance;
    }

    private RcpServicesCallerSingleton() throws IOException {
        ServerConfigSingleton serverConfig = ServerConfigSingleton.getInstance();
        name2service = new HashMap<>();
        try {
            for (String serviceName : serverConfig.getServiceNameSet()) {
                String serviceClassName = serverConfig.getServiceClassName(serviceName);
                Class<?> clazz = Class.forName(serviceClassName);
                Constructor<?> ctor = clazz.getConstructor();
                Object serviceObject = ctor.newInstance();
                name2service.put(serviceName, serviceObject);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public ResultAndType invokeServiceMethod(String serviceName, String methodName, Object[] params)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchService {
        if (!name2service.containsKey(serviceName)) {
            throw new NoSuchService(serviceName);
        }
        Object service = name2service.get(serviceName);
        Class[] paramTypes = getParamTypes(params);

        Method serviceMethod = service.getClass().getDeclaredMethod(methodName, paramTypes);
        Object result = serviceMethod.invoke(service, params);
        return new ResultAndType(result, serviceMethod.getReturnType());
    }

    private Class[] getParamTypes(Object[] params) {
        Class[] paramTypes = new Class[params.length];
        Arrays.setAll(paramTypes, (i) -> params[i].getClass());
        return paramTypes;
    }

    /**
     * Class contains result and return type of service method
     */
    public class ResultAndType {
        private Object result;
        private Class resultType;

        public ResultAndType(Object result, Class resultType) {
            this.result = result;
            this.resultType = resultType;
        }

        public Object getResult() {
            return result;
        }

        public Class getResultType() {
            return resultType;
        }
    }
}
