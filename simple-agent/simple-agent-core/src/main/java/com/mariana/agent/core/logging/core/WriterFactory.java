package com.mariana.agent.core.logging.core;

import com.google.common.base.Strings;
import com.mariana.agent.core.boot.AgentPackageNotFoundException;
import com.mariana.agent.core.boot.AgentPackagePath;
import com.mariana.agent.core.config.Config;
import com.mariana.agent.core.config.AgentConfigInitializer;
import com.mariana.agent.core.plugin.PluginFinder;

public class WriterFactory {

    private static IWriter WRITER;

    public static IWriter getLogWriter() {

        switch (Config.Logging.OUTPUT) {
            case FILE:
                if (null != WRITER) {
                    return WRITER;
                }

                if (isAgentInitCompleted()) {
                    if (Strings.isNullOrEmpty(Config.Logging.DIR)) {
                        try {
                            Config.Logging.DIR = AgentPackagePath.getPath() + "/logs";
                        } catch (AgentPackageNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    WRITER = FileWriter.get();
                } else {
                    WRITER = SystemOutWriter.INSTANCE;
                }

                break;

            default:
                return SystemOutWriter.INSTANCE;
        }

        return WRITER;
    }

    private static boolean isAgentInitCompleted() {
        return AgentConfigInitializer.isIsInitCompleted()
                && PluginFinder.isIsPluginInitCompleted()
                && AgentPackagePath.isPathFound();
    }

}
