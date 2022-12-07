package com.mariana.agent.core.config;

import com.mariana.agent.core.logging.api.ILog;
import com.mariana.agent.core.logging.api.LogManager;

import java.io.InputStreamReader;
import java.util.Properties;

public class ConfigInitializer {

    private static final ILog LOGGER = LogManager.getLogger(ConfigInitializer.class);

    private static final String ENV_KEY_SPECIFIED_CONFIG_PATH = "agent_config";
    private static boolean IS_INIT_COMPLETED = false;
    private static Properties AGENT_SETTINGS;

    public static boolean isIsInitCompleted() {
        return IS_INIT_COMPLETED;
    }

    public static void initializeConfig(String args) {

        AGENT_SETTINGS = new Properties();
        // override config by agent.config

        // override config by system env

        // override config by agent args

        IS_INIT_COMPLETED = true;
    }

    private static InputStreamReader loadConfig() {

    }

}
