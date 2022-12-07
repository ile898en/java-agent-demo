package com.mariana.agent.core.logging.core.converters;

import com.mariana.agent.core.config.Config;
import com.mariana.agent.core.logging.core.Converter;
import com.mariana.agent.core.logging.core.LogEvent;

public class AgentNameConverter implements Converter {

    @Override
    public String convert(LogEvent logEvent) {
        return Config.Agent.SERVICE_NAME;
    }

    @Override
    public String getKey() {
        return "agent_name";
    }
}
