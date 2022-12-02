package com.mariana.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class DemoAppService {

    private static final Logger logger = LoggerFactory.getLogger(DemoAppService.class);

    public String hello() {
        int randomInt = new Random().nextInt(500);
        try {
            TimeUnit.MILLISECONDS.sleep(randomInt);
        } catch (InterruptedException e) {
            logger.error("DemoAppService#sleep() throws exception: ", e);
            throw new RuntimeException("DemoAppService#sleep() throws exception " + e.getMessage());
        }
        return "hello world.";
    }


}
