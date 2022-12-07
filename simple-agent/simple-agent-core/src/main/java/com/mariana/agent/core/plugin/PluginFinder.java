package com.mariana.agent.core.plugin;

public class PluginFinder {

    private static boolean IS_PLUGIN_INIT_COMPLETED = false;

    public static void pluginInitCompleted() {
        IS_PLUGIN_INIT_COMPLETED = true;
    }

    public static boolean isIsPluginInitCompleted() {
        return IS_PLUGIN_INIT_COMPLETED;
    }

}
