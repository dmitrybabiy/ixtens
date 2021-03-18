package com.mycompany.testtask.rcpclient;

import com.mycompany.testtask.rcpclient.exception.RemoteCallException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Class launcher for client requests
 * <p>
 * Created by Dima on 26.08.2016.
 */
public class ClientLauncher {
    private final static Logger logger = LogManager.getLogger(ClientLauncher.class);
    public static void main(String[] args) throws IOException, InterruptedException {
        int port = Integer.parseInt(args[0]);

        ExecutorService executorCaller = Executors.newCachedThreadPool();

        Client client = new Client("localhost", port);

        System.out.println("client started");

        for (int i = 0; i < 3; i++) {
            executorCaller.submit(new Caller(client));
        }

        executorCaller.shutdown();
        executorCaller.awaitTermination(30, TimeUnit.SECONDS);
        client.close();
    }

    private static class Caller implements Runnable {
        private Client c;

        public Caller(Client c) {
            this.c = c;
        }

        public void run() {
            try {
//            c.remoteCall("service1", "sum", new Object[] {new Long(10), new Long(20)});
            while (true) {
            c.remoteCall("service1", "sleep", new Object[]{new Long(10)});
//                c.remoteCall("service1", "sleep", new Object[]{new Double(1000)});
//                c.remoteCall("service2", "sleep", new Object[]{new Double(1000)});
                for (int i = 0; i < 5; i++) {
                    c.remoteCall("service1", "sum", new Object[]{new Long(10 * i), new Long(20)});
                }
            }
//            c.remoteCall("service1", "sleep", new Object[] {new Double(1000)});
//            c.remoteCall("service1", "exception", new Object[] {});
//            for (int i = 0; i < 5; i++) {
//                c.remoteCall("service1", "sum", new Object[] {new Long(10 * i), new Long(20)});
//            }
            } catch (RemoteCallException e) {
                logger.error("", e);
            }
        }
    }
}
