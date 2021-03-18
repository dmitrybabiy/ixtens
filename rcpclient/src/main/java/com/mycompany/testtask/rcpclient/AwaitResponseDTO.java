package com.mycompany.testtask.rcpclient;

import com.mycompany.testtask.rcpapi.ResponseDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CountDownLatch;

/**
 * Class provides awaiting response
 *
 * Created by Dima on 30.08.2016.
 */
public class AwaitResponseDTO {
    private static final Logger logger = LogManager.getLogger(AwaitResponseDTO.class);
    private ResponseDTO responseDTO;
    private CountDownLatch cdl;

    public AwaitResponseDTO() {
        responseDTO = null;
        cdl = new CountDownLatch(1);

    }

    public void setResponseDTO(ResponseDTO responseDTO) {
        this.responseDTO = responseDTO;
        cdl.countDown();
    }

    public ResponseDTO getResponseDTO() {
        try {
            cdl.await();
        } catch (InterruptedException e) {
            logger.error("", e);
            return null;
        }
        return responseDTO;
    }

    public void release() {
        cdl.countDown();
    }
}
