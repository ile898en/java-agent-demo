package com.mariana.agent.core.logging.api;

public interface LogResolver {

    ILog getLogger(Class<?> clazz);

    ILog getLogger(String clazz);

}
