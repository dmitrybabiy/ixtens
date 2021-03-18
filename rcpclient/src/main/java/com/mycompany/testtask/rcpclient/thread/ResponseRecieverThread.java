package com.mycompany.testtask.rcpclient.thread;

import com.mycompany.testtask.rcpapi.ResponseDTO;
import com.mycompany.testtask.rcpclient.AwaitResponseDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

/**
 * Class thread listens socket input stream and stores responses
 *
 * Created by Dima on 28.08.2016.
 */
public class ResponseRecieverThread implements Runnable {
    private final static Logger logger = LogManager.getLogger(ResponseRecieverThread.class);
    final private Socket clientSocket;
    private ConcurrentMap<Long, AwaitResponseDTO> responseMap;
    private final Object islock = new Object();

    public ResponseRecieverThread(Socket clientSocket, ConcurrentMap<Long, AwaitResponseDTO> responseMap) {
        this.clientSocket = clientSocket;
        this.responseMap = responseMap;
    }

    @Override
    public void run() {
        logger.debug("response reciever started");
        try (ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream())) {
            while (!clientSocket.isClosed()) {
                ResponseDTO responseDTO = null;
                synchronized (islock) {
                    try {
                        responseDTO = (ResponseDTO) ois.readObject();
                    } catch (EOFException e) {
                        logger.debug("EOF reached");
                        break;
                    }
                }

                if (responseDTO != null) {
                    Long responseId = responseDTO.getId();
                    if (!responseMap.containsKey(responseId)) {
                        logger.error("response error, id {} wasn't send", responseId);
                    } else {
                        responseMap.get(responseId).setResponseDTO(responseDTO);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            if (!clientSocket.isClosed()) {
                logger.error("reciever io exception", e);
            }
        }
        logger.debug("response reciever finished");
    }
}
