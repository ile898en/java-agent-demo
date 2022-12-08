package com.mariana.agent.starter;

import com.mariana.agent.core.config.AgentConfigInitializer;
import com.mariana.agent.core.logging.api.ILog;
import com.mariana.agent.core.logging.api.LogManager;

import java.lang.instrument.Instrumentation;

public class AgentStarter {

    private static final ILog LOGGER = LogManager.getLogger(AgentStarter.class);

    public static void premain(String args, Instrumentation instrumentation) {
        LOGGER.info("AgentStarter#premain starting with args {}", args);
        // init config
        AgentConfigInitializer.initialize(args);

        // load plugins

        // add transformers

    }

    public static void agentmain(String args, Instrumentation instrumentation) {
        LOGGER.info("AgentStarter#agentmain starting with args {}", args);
    }

}
