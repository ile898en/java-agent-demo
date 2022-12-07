package com.mariana.agent.core.config;

public class ConfigInitializer {

    private static boolean IS_INIT_COMPLETED = false;

    public static void initializeConfig(String args) {

        // override config by agent.config

        // override config by system env

        // override config by agent args

        IS_INIT_COMPLETED = true;
    }

    public static boolean isIsInitCompleted() {
        return IS_INIT_COMPLETED;
    }

}
