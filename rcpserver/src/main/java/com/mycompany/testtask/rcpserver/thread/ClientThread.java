package com.mycompany.testtask.rcpserver.thread;

import com.mycompany.testtask.rcpapi.CommandDTO;
import com.mycompany.testtask.rcpapi.ResponseDTO;
import com.mycompany.testtask.rcpserver.RcpServicesCallerSingleton;
import com.mycompany.testtask.rcpserver.exception.NoSuchService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Class client thread for one accept
 * <p>
 * Created by Dima on 26.08.2016.
 */
public class ClientThread implements Runnable {
    private static final Logger logger = LogManager.getLogger(ClientThread.class);
    private static final int THREAD_POOL_SIZE = 10;
    private Socket clientSocket;
    private final Object islock = new Object();
    private final Object oslock = new Object();

    public ClientThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        logger.debug("client thread started");
        ExecutorService executorServiceMethod = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        try (final ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
             final ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream())) {
            while (true) {
                final CommandDTO commandDTO;
                synchronized (islock) {
                    try {
                        commandDTO = (CommandDTO) ois.readObject();
                        logger.debug("recieved command : {}", commandDTO);
                    } catch (EOFException e) {
                        logger.error("EOF reached", e);
                        break;
                    } catch (ClassNotFoundException e) {
                        logger.error("", e);
                        break;
                    }
                }
                CompletableFuture.supplyAsync(new Supplier<ResponseDTO>() {
                    @Override
                    public ResponseDTO get() {
                        return callServiceMethod4DTO(commandDTO);
                    }
                }, executorServiceMethod).thenAcceptAsync(new Consumer<ResponseDTO>() {
                    @Override
                    public void accept(ResponseDTO rcpResponseDTO) {
                        sendResponse(oos, rcpResponseDTO);
                    }
                }, executorServiceMethod);
            }
        } catch (IOException e) {
            logger.error("client io exception", e);
        } finally {
            if (executorServiceMethod != null) {
                executorServiceMethod.shutdown();
                try {
                    executorServiceMethod.awaitTermination(30, TimeUnit.SECONDS);
                    executorServiceMethod.shutdownNow();
                } catch (InterruptedException e) {
                    logger.error(e);
                }
            }
        }
        logger.debug("client thread finished : {} ");
    }

    private ResponseDTO callServiceMethod4DTO(CommandDTO commandDTO) {
        RcpServicesCallerSingleton rcpCaller = RcpServicesCallerSingleton.getInstance();
        logger.debug("service method, command {}", commandDTO);
        RcpServicesCallerSingleton.ResultAndType resultAndType = null;
        String errorMessage = null;
        try {
            resultAndType = rcpCaller.invokeServiceMethod(commandDTO.getServiceName(),
                    commandDTO.getMethodName(), commandDTO.getParams());
        } catch (NoSuchMethodException e) {
            logger.error("", e);
            errorMessage = "no such method " + e.getMessage();
        } catch (InvocationTargetException e) {
            logger.error("", e);
            logger.error("", e.getCause());
            errorMessage = "invocation exception : " + e.getCause().getStackTrace()[0].getClassName() + " : " +
                    e.getCause().getMessage();
        } catch (IllegalAccessException e) {
            logger.error("", e);
            errorMessage = "illegal access exception" + e.getMessage();
        } catch (NoSuchService e) {
            logger.error("", e);
            errorMessage = "wrong service name " + e.getMessage();
        }
        ResponseDTO responseDTO = (resultAndType == null) ?
                new ResponseDTO(commandDTO.getId(), null, null, errorMessage) :
                new ResponseDTO(commandDTO.getId(), resultAndType.getResult(),
                        resultAndType.getResultType().equals(Void.TYPE), null);
        logger.debug("service method, response {}", responseDTO);
        return responseDTO;
    }

    private void sendResponse(ObjectOutputStream oos, ResponseDTO responseDTO) {
        synchronized (oslock) {
            try {
                oos.writeObject(responseDTO);
            } catch (IOException e) {
                logger.error("unable to send response", e);
            }
        }
    }
}
