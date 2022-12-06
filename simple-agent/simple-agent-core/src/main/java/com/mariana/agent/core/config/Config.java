package com.mariana.agent.core.config;

import com.mariana.agent.core.logging.core.LogLevel;
import com.mariana.agent.core.logging.core.LogOutput;
import com.mariana.agent.core.logging.core.ResolverType;

public class Config {

    public static class Agent {
        public static String SERVICE_NAME = "";
    }

    public static class Logging {
        // 日志文件目录，默认使用"{agent_jar_dir}/logs/"
        public static String DIR = "";
        // 日志文件名
        public static String FILE_NAME = "agent.log";
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
    }

    public static class Jvm {

    }

    public static class Plugin {

    }

}
