package com.mariana.agent.core.config;

import com.alibaba.fastjson.JSON;
import com.google.common.primitives.Chars;
import com.mariana.agent.common.util.Length;
import com.mariana.agent.core.logging.core.LogLevel;
import com.mariana.agent.core.logging.core.LogOutput;
import com.mariana.agent.core.logging.core.ResolverType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Config {

    public static class Agent {
        @Length(20)
        public static String NAMESPACE = "";
        @Length(50)
        public static String SERVICE_NAME = "";
        @Length(20)
        public static String CLUSTER = "";
        @Length(50)
        public volatile static String INSTANCE_NAME = "";
    }

    public static class Collector {
        public static String BACKEND_SERVICE = "";
    }

    public static class Logging {
        // 日志文件目录，默认使用"{agent_jar_dir}/logs/"
        public static String DIR = "";
        // 日志文件名
        public static String FILE_NAME = "simple-agent.log";
        // 日志文件默认大小
        public static int MAX_FILE_SIZE = 300 * 1024 * 1024;
        // 默认日志级别
        public static LogLevel LEVEL = LogLevel.DEBUG;
        // 默认日志输出：文件或控制台
        public static LogOutput OUTPUT = LogOutput.FILE;
        // 默认的日志解析器类型
        public static ResolverType RESOLVER = ResolverType.PATTERN;
        // 默认的日志Pattern
        public static String PATTERN = "%level %timestamp %thread %class : %msg %throwable";
        // 默认 最多保留多少个日志文件
        public static int MAX_HISTORY_FILES = -1;
    }

    public static class Jvm {
        /**
         * The buffer size of collected JVM info.
         */
        public static int BUFFER_SIZE = 60 * 10;
    }

    public static class Plugin {
        /**
         * Control the length of the peer field.
         */
        public static int PEER_MAX_LENGTH = 200;

        /**
         * Exclude activated plugins
         */
        public static String EXCLUDE_PLUGINS = "";

        /**
         * Mount the folders of the plugins. The folder path is relative to agent.jar.
         */
        public static List<String> MOUNT = Arrays.asList("plugins", "activations");
    }

    public static void print() {
        final String rootName = Config.class.getSimpleName();
        final CharSequence c = ".";

        Map<String, Object> configMap = new HashMap<>();

        Class<?>[] classes = Config.class.getClasses();

        for (Class<?> aClass : classes) {
            Field[] fields = aClass.getFields();
            for (Field field : fields) {
                int mod = field.getModifiers();
                if (Modifier.isPublic(mod) && Modifier.isStatic(mod)) {
                    String key = rootName + c + field.getName();
                    try {
                        Object value = field.get(null);
                        configMap.put(key, value);
                    } catch (IllegalAccessException e) {
                        System.err.println("Config field `" + key + "` get error. " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }

        System.out.println(JSON.toJSONString(configMap));
    }

}
