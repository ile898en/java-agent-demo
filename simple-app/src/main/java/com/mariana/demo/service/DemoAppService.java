package com.mariana.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class DemoAppService {

    private static final Logger logger = LoggerFactory.getLogger(DemoAppService.class);

    public String hello(String requesterId) {
        logger.info("DemoAppService#hello: {} say hello.", requesterId);
        int randomInt = new Random().nextInt();
        try {
            TimeUnit.MICROSECONDS.sleep(randomInt);
        } catch (InterruptedException e) {
            logger.error("DemoAppService#hello: sleep() error. ", e);
            throw new RuntimeException("DemoAppService#hello: sleep() error " + e.getMessage());
        }
        return "hello world.";
    }

}
