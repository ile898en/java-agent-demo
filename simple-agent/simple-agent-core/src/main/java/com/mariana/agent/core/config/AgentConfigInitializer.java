package com.mariana.agent.core.config;

import com.google.common.base.Strings;
import com.mariana.agent.core.boot.AgentPackageNotFoundException;
import com.mariana.agent.core.boot.AgentPackagePath;
import com.mariana.agent.core.logging.api.ILog;
import com.mariana.agent.core.logging.api.LogManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.StringJoiner;

public class AgentConfigInitializer {

    private static final ILog LOGGER = LogManager.getLogger(AgentConfigInitializer.class);

    private static final String ENV_KEY_SPECIFIED_CONFIG_PATH = "agent_config";
    private static final String DEFAULT_AGENT_FILE_PATH = "/config/agent.config";
    private static boolean IS_INIT_COMPLETED = false;
    private static Properties AGENT_SETTINGS;

    public static boolean isIsInitCompleted() {
        return IS_INIT_COMPLETED;
    }

    /**
     * SkyWalking是先从配置文件加载配置，然后从系统环境变量覆盖配置，最后从agent启动参数覆盖配置，即：
     * 配置优先级：agent.config < System Properties < Agent Options
     * 为了简单起见，这里仅支持从agent.config文件中读取配置
     */
    public static void initialize(String args) {

        AGENT_SETTINGS = new Properties();
        // override config by agent.config
        try (InputStreamReader configFileStream = loadConfig()) {
            AGENT_SETTINGS.load(configFileStream);
            // SkyWalking的配置文件里是支持占位符`${}`的，解析起来比较麻烦;
            // 简单起见，我们这里直接不支持占位符，哈哈
        } catch (Exception e) {
            LOGGER.error("Failed to read the config file, continue to use default config.");
        }

        // override config by system env (unimplemented)
        // override config by agent args (unimplemented)

        initializeConfig(Config.class);

        // check field `service_name`
        if (Strings.isNullOrEmpty(Config.Agent.SERVICE_NAME)) {
            throw new ExceptionInInitializerError("`agent.service_name` missing.");
        } else {
            if (Strings.isNullOrEmpty(Config.Agent.NAMESPACE) || Strings.isNullOrEmpty(Config.Agent.CLUSTER)) {
                Config.Agent.SERVICE_NAME = new StringJoiner("|")
                        .add(Config.Agent.SERVICE_NAME)
                        .add(Config.Agent.NAMESPACE)
                        .add(Config.Agent.CLUSTER)
                        .toString();
            }
        }

//        // check field `backend_service`: 暂时还没有AgentServer，暂时不校验此项
//        if (Strings.isNullOrEmpty(Config.Collector.BACKEND_SERVICE)) {
//            throw new ExceptionInInitializerError("`collector.backend_service` is missing.");
//        }

        IS_INIT_COMPLETED = true;
    }

    public static void initializeConfig(Class<? extends Config> configClass) {
        if (AGENT_SETTINGS == null) {
            LOGGER.error("Plugin configs have to be initialized after core config initialization.");
            return;
        }
        try {
            ConfigInitializer.initialize(AGENT_SETTINGS, configClass);
        } catch (IllegalAccessException e) {
            LOGGER.error(e,
                    "Failed to set the agent settings {}"
                            + " to Config={} ",
                    AGENT_SETTINGS, configClass
            );
        }
    }

    private static InputStreamReader loadConfig() throws AgentPackageNotFoundException, ConfigNotFoundException {
        String specifiedConfigPath = System.getProperty(ENV_KEY_SPECIFIED_CONFIG_PATH);
        File configFile = Strings.isNullOrEmpty(specifiedConfigPath)
                ? new File(AgentPackagePath.getPath(), DEFAULT_AGENT_FILE_PATH) : new File(specifiedConfigPath);

        if (configFile.exists() && configFile.isFile()) {
            try {
                LOGGER.info("config file found in {}", configFile);
                return new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8);
            } catch (FileNotFoundException e) {
                throw new ConfigNotFoundException("Failed to load agent.config", e);
            }
        }
        throw new ConfigNotFoundException("Failed to load agent.config");
    }


}
