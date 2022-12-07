package com.mariana.agent.core.config;

import com.google.common.base.Strings;
import com.mariana.agent.common.util.Length;
import com.mariana.agent.core.boot.AgentPackageNotFoundException;
import com.mariana.agent.core.boot.AgentPackagePath;
import com.mariana.agent.core.logging.api.ILog;
import com.mariana.agent.core.logging.api.LogManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ConfigInitializer {

    private static final ILog LOGGER = LogManager.getLogger(ConfigInitializer.class);

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

        // check the service name
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

//        // check the backend service: 暂时还没有AgentServer，暂时不校验此项
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

    private static void initialize(Properties properties, Class<?> rootConfigType) throws IllegalAccessException {
        initNextLevel(properties, rootConfigType, new ConfigDesc());
    }

    private static void initNextLevel(Properties properties, Class<?> recentConfigType,
                                      ConfigDesc parentDesc) throws IllegalArgumentException, IllegalAccessException {
        for (Field field : recentConfigType.getFields()) {
            if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
                String configKey = (parentDesc + "." + field.getName()).toLowerCase();
                Class<?> type = field.getType();
                if (Map.class.isAssignableFrom(type)) {
                    /*
                     * Map config format is, config_key[map_key]=map_value, such as plugin.opgroup.resttemplate.rule[abc]=/url/path
                     * "config_key[]=" will generate an empty Map , user could use this mechanism to set an empty Map
                     */
                    ParameterizedType genericType = (ParameterizedType) field.getGenericType();
                    Type[] argumentTypes = genericType.getActualTypeArguments();
                    Type keyType = argumentTypes[0];
                    Type valueType = argumentTypes[1];
                    // A chance to set an empty map
                    if (properties.containsKey(configKey + "[]")) {
                        Map currentValue = (Map) field.get(null);
                        if (currentValue != null && !currentValue.isEmpty()) {
                            field.set(null, initEmptyMap(type));
                        }
                    } else {
                        // Set the map from config key and properties
                        Map map = readMapType(type, configKey, properties, keyType, valueType);
                        if (map.size() != 0) {
                            field.set(null, map);
                        }
                    }
                } else if (properties.containsKey(configKey)) {
                    //In order to guarantee the default value could be reset as empty , we parse the value even if it's blank
                    String propertyValue = properties.getProperty(configKey, "");
                    if (Collection.class.isAssignableFrom(type)) {
                        ParameterizedType genericType = (ParameterizedType) field.getGenericType();
                        Type argumentType = genericType.getActualTypeArguments()[0];
                        Collection collection = convertToCollection(argumentType, type, propertyValue);
                        field.set(null, collection);
                    } else {
                        // Convert the value into real type
                        final Length lengthDefine = field.getAnnotation(Length.class);
                        if (lengthDefine != null && propertyValue.length() > lengthDefine.value()) {
                            if (Strings.isNullOrEmpty(propertyValue)) {
                                propertyValue = Strings.nullToEmpty(propertyValue);
                            } else {
                                propertyValue = propertyValue.substring(0, lengthDefine.value());
                            }
                            System.err.printf("The config value will be truncated , because the length max than %d : %s -> %s%n", lengthDefine.value(), configKey, propertyValue);
                        }
                        Object convertedValue = convertToTypicalType(type, propertyValue);
                        if (convertedValue != null) {
                            field.set(null, convertedValue);
                        }
                    }
                }

            }
        }
        for (Class<?> innerConfiguration : recentConfigType.getClasses()) {
            parentDesc.append(innerConfiguration.getSimpleName());
            initNextLevel(properties, innerConfiguration, parentDesc);
            parentDesc.removeLastDesc();
        }
    }

    private static Collection<Object> convertToCollection(Type argumentType, Class<?> type, String propertyValue) {
        Collection<Object> collection;
        if (type.equals(Set.class) || type.equals(HashSet.class)) {
            collection = new HashSet<>();
        } else if (type.equals(TreeSet.class)) {
            collection = new TreeSet<>();
        } else if (type.equals(List.class) || type.equals(LinkedList.class)) {
            collection = new LinkedList<>();
        } else if (type.equals(ArrayList.class)) {
            collection = new ArrayList<>();
        } else {
            throw new UnsupportedOperationException("Config parameter type support Set,HashSet,TreeSet,List,LinkedList,ArrayList");
        }
        if (Strings.isNullOrEmpty(propertyValue) || Strings.isNullOrEmpty(propertyValue.trim())) {
            return collection;
        }
        Arrays.stream(propertyValue.split(","))
                .map(v -> convertToTypicalType(argumentType, v))
                .forEach(collection::add);
        return collection;
    }


    /**
     * Convert string value to typical type.
     *
     * @param type  type to convert
     * @param value string value to be converted
     * @return converted value or null
     */
    private static Object convertToTypicalType(Type type, String value) {
        if (Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(value.trim())) {
            return null;
        }
        Object result = null;
        if (String.class.equals(type)) {
            result = value;
        } else if (int.class.equals(type) || Integer.class.equals(type)) {
            result = Integer.valueOf(value);
        } else if (long.class.equals(type) || Long.class.equals(type)) {
            result = Long.valueOf(value);
        } else if (boolean.class.equals(type) || Boolean.class.equals(type)) {
            result = Boolean.valueOf(value);
        } else if (float.class.equals(type) || Float.class.equals(type)) {
            result = Float.valueOf(value);
        } else if (double.class.equals(type) || Double.class.equals(type)) {
            result = Double.valueOf(value);
        } else if (type instanceof Class) {
            Class<?> clazz = (Class<?>) type;
            if (clazz.isEnum()) {
                result = Enum.valueOf((Class<Enum>) type, value.toUpperCase());
            }
        }
        return result;
    }

    /**
     * Set map items.
     *
     * @param type       the filed type
     * @param configKey  config key must not be null
     * @param properties properties must not be null
     * @param keyType    key type of the map
     * @param valueType  value type of the map
     */
    private static Map readMapType(Class<?> type,
                                   String configKey,
                                   Properties properties,
                                   final Type keyType,
                                   final Type valueType) {

        Objects.requireNonNull(configKey);
        Objects.requireNonNull(properties);
        Map<Object, Object> map = initEmptyMap(type);
        String prefix = configKey + "[";
        String suffix = "]";
        properties.forEach((propertyKey, propertyValue) -> {
            String propertyStringKey = propertyKey.toString();
            if (propertyStringKey.startsWith(prefix) && propertyStringKey.endsWith(suffix)) {
                String itemKey = propertyStringKey.substring(
                        prefix.length(), propertyStringKey.length() - suffix.length());
                Object keyObj;
                Object valueObj;

                keyObj = convertToTypicalType(keyType, itemKey);
                valueObj = convertToTypicalType(valueType, propertyValue.toString());

                if (keyObj == null) {
                    keyObj = itemKey;
                }

                if (valueObj == null) {
                    valueObj = propertyValue;
                }
                map.put(keyObj, valueObj);
            }
        });
        return map;
    }

    private static Map<Object, Object> initEmptyMap(Class<?> type) {
        if (type.equals(Map.class) || type.equals(HashMap.class)) {
            return new HashMap<>();
        } else if (type.equals(TreeMap.class)) {
            return new TreeMap<>();
        } else {
            throw new UnsupportedOperationException("Config parameter type support Map,HashMap,TreeMap");
        }
    }

}

class ConfigDesc {
    private LinkedList<String> descs = new LinkedList<>();

    void append(String currentDesc) {
        if (Strings.isNullOrEmpty(currentDesc)) {
            descs.addLast(currentDesc);
        }
    }

    void removeLastDesc() {
        descs.removeLast();
    }

    @Override
    public String toString() {
        return String.join(".", descs);
    }
}
