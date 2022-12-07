package com.mariana.agent.core.logging.core;

import com.mariana.agent.core.config.Config;
import com.mariana.agent.core.logging.api.ILog;
import com.mariana.agent.core.logging.api.LogResolver;

public class PatternLogResolver implements LogResolver {
    @Override
    public ILog getLogger(Class<?> clazz) {
        return new PatternLogger(clazz, Config.Logging.PATTERN);
    }

    @Override
    public ILog getLogger(String clazz) {
        return new PatternLogger(clazz, Config.Logging.PATTERN);
    }
}
