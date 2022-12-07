package com.mariana.agent.core.logging.api;

import com.mariana.agent.core.logging.core.PatternLogResolver;

public class LogManager {

    private static LogResolver RESOLVER = new PatternLogResolver();

    public static void setLogResolver(LogResolver resolver) {
        LogManager.RESOLVER = resolver;
    }

    public static ILog getLogger(Class<?> clazz) {
        if (RESOLVER == null) {
            return NoopLogger.INSTANCE;
        }
        return LogManager.RESOLVER.getLogger(clazz);
    }

    public static ILog getLogger(String clazz) {
        if (RESOLVER == null) {
            return NoopLogger.INSTANCE;
        }
        return LogManager.RESOLVER.getLogger(clazz);
    }

}
