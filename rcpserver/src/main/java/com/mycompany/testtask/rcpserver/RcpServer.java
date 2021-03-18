package com.mycompany.testtask.rcpserver;

import com.mycompany.testtask.rcpserver.thread.ClientThread;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Class server accepts connections from clients
 * <p>
 * Created by Dima on 26.08.2016.
 */
public class RcpServer {
    private static final Logger logger = LogManager.getLogger(RcpServer.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = Integer.parseInt(args[0]);

        RcpServicesCallerSingleton.getInstance();

        ExecutorService executorClient = null;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.debug("server started");
            System.out.println("server started");
            executorClient = Executors.newCachedThreadPool();
            while (true) {
                Socket clientSocket = serverSocket.accept();

                logger.debug("client accepted");
                ClientThread cliThread = new ClientThread(clientSocket);
                executorClient.submit(cliThread);
            }
        } catch (IOException e) {
            logger.error("unable to start server", e);
            throw e;
        } finally {
            if (executorClient != null) {
                executorClient.shutdown();
                executorClient.awaitTermination(30, TimeUnit.MINUTES.SECONDS);
                executorClient.shutdownNow();
            }
        }
    }
}
