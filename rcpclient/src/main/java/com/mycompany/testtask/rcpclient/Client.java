package com.mycompany.testtask.rcpclient;

import com.mycompany.testtask.rcpapi.CommandDTO;
import com.mycompany.testtask.rcpapi.ResponseDTO;
import com.mycompany.testtask.rcpclient.exception.RemoteCallException;
import com.mycompany.testtask.rcpclient.thread.ResponseRecieverThread;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Class client sends and recieves remote call procedures
 * <p>
 * Created by Dima on 26.08.2016.
 */
public class Client {
    private final static Logger logger = LogManager.getLogger(Client.class);
    private static final int ATTEMPT_COUNT = 3;
    private static AtomicLong atomicRequestId = new AtomicLong(0);
    private String host;
    private int port;
    private Socket clientSocket = null;
    private ObjectOutputStream oos = null;

    private Thread responseRecieverThread = null;
    private ConcurrentMap<Long, AwaitResponseDTO> responseMap;
    private final Object oslock = new Object();

    public Client(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        reconnect();
    }

    private ResponseDTO tryRemoteCall(CommandDTO commandDTO) throws IOException, ClassNotFoundException,
            InterruptedException {
        synchronized (oslock) {
            responseMap.put(commandDTO.getId(), new AwaitResponseDTO());
            oos.writeObject(commandDTO);
            logger.debug("command  sent : {}", commandDTO);
        }
        ResponseDTO responseDTO = null;
        responseDTO = responseRecieve(commandDTO.getId());
        logger.debug("response recieved  id : {}, dto : {}", commandDTO.getId(), responseDTO);
        return responseDTO;
    }

    public Object remoteCall(String serviceName, String methodName, Object[] params) {
        if (clientSocket.isClosed()) {
            reconnect();
        }

        long requestId = atomicRequestId.addAndGet(1);
        CommandDTO commandDTO = new CommandDTO(requestId, serviceName, methodName, params);
        ResponseDTO responseDTO = null;

        for (int i = 0; i < ATTEMPT_COUNT; i++) {
            try {
                responseDTO = tryRemoteCall(commandDTO);
                if (responseDTO != null) {
                    break;
                } else {
                    Thread.sleep(10);
                }
            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                logger.error("bad attempt", e);
                reconnect();
            }
        }

        if (responseDTO == null) {
            throw new RemoteCallException("no response for request " + requestId);
        }

        String errorMessage = responseDTO.getErrorMessage();
        if (errorMessage != null) {
            throw new RemoteCallException(errorMessage);
        } else {
            return responseDTO.getResult();
        }
    }

    private void reconnect() {
        synchronized (this) {
            try {
                if (clientSocket != null) {
                    if (!clientSocket.isClosed()) {
                        return;
                    }
                    clientSocket.close();
                }
                logger.debug("try to reconnect");
                if (responseRecieverThread != null) {
                    responseRecieverThread.interrupt();
                }
                if (responseMap != null) {
                    responseMap.values().forEach((i) -> i.release());
                }
                responseMap = new ConcurrentHashMap<>();
                clientSocket = new Socket(host, port);
                oos = new ObjectOutputStream(clientSocket.getOutputStream());
                responseRecieverThread = new Thread(new ResponseRecieverThread(clientSocket, responseMap));
                responseRecieverThread.start();
            } catch (IOException e) {
                logger.error("unable to reconnect", e);
                throw new RuntimeException(e);
            }
        }
    }

    private ResponseDTO responseRecieve(Long responseId) throws InterruptedException {
        ResponseDTO responseDTO = responseMap.get(responseId).getResponseDTO();
        responseMap.remove(responseId);
        return responseDTO;
    }

    public void close() throws IOException {
        // responseRecieverThread will be automatically finished after socket closing
        if (clientSocket != null && !clientSocket.isClosed()) {
            clientSocket.close();
        }
    }
}
