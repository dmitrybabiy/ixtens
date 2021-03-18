package com.mycompany.testtask.rcpserver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

/**
 * Class provides access to server config
 * <p>
 * Created by Dima on 28.08.2016.
 */
public class ServerConfigSingleton {
    private static ServerConfigSingleton instance;
    private static final String SERVICE_PACKAGE = "com.mycompany.testtask.rcpserver.service";
    private static final String CONFIG_FILE = "/server.properties";
    Properties properties = null;

    static {
        if (instance == null) {
            try {
                instance = new ServerConfigSingleton();
            } catch (IOException e) {
                throw new ExceptionInInitializerError(e);
            }
        }
    }

    public static ServerConfigSingleton getInstance() {
        return instance;
    }

    private ServerConfigSingleton() throws IOException {
        try (InputStream is = getClass().getResourceAsStream(CONFIG_FILE)) {
            properties = new Properties();
            properties.load(is);
        }
    }

    public Set<String> getServiceNameSet() {
        return properties.stringPropertyNames();
    }

    public String getServiceClassName(String serviceName) {
        String serviceClassName = properties.getProperty(serviceName);
        return SERVICE_PACKAGE + "." + serviceClassName;
    }
}
