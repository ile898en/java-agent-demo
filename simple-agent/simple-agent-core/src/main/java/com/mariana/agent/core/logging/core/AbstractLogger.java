package com.mariana.agent.core.logging.core;

import com.mariana.agent.core.config.Config;
import com.mariana.agent.core.logging.api.ILog;
import com.mariana.agent.core.logging.core.converters.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public abstract class AbstractLogger implements ILog {

    public static final Map<String, Class<? extends Converter>> DEFAULT_CONVERTER_MAP = new HashMap<>();
    protected List<Converter> converters = new ArrayList<>();

    static {
        DEFAULT_CONVERTER_MAP.put("thread", ThreadConverter.class);
        DEFAULT_CONVERTER_MAP.put("level", LevelConverter.class);
        DEFAULT_CONVERTER_MAP.put("agent_name", AgentNameConverter.class);
        DEFAULT_CONVERTER_MAP.put("timestamp", DateConverter.class);
        DEFAULT_CONVERTER_MAP.put("msg", MessageConverter.class);
        DEFAULT_CONVERTER_MAP.put("throwable", ThrowableConverter.class);
        DEFAULT_CONVERTER_MAP.put("class", ClassConverter.class);
    }

    protected final String targetClass;

    public AbstractLogger(String targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public void info(String format) {
        if (this.isInfoEnable()) {
            this.logger(LogLevel.INFO, format, null);
        }
    }

    @Override
    public void info(String format, Object... arguments) {
        if (this.isInfoEnable()) {
            this.logger(LogLevel.INFO, replaceParam(format, arguments), null);
        }
    }

    @Override
    public void info(Throwable t, String format, Object... arguments) {
        if (this.isInfoEnable()) {
            this.logger(LogLevel.INFO, replaceParam(format, arguments), t);
        }
    }

    @Override
    public void warn(String format, Object... arguments) {
        if (this.isWarnEnable()) {
            this.logger(LogLevel.WARN, replaceParam(format, arguments), null);
        }
    }

    @Override
    public void warn(Throwable e, String format, Object... arguments) {
        if (this.isWarnEnable()) {
            this.logger(LogLevel.WARN, replaceParam(format, arguments), e);
        }
    }

    @Override
    public void error(String format, Throwable e) {
        if (this.isErrorEnable()) {
            this.logger(LogLevel.ERROR, format, e);
        }
    }

    @Override
    public void error(Throwable e, String format, Object... arguments) {
        if (this.isErrorEnable()) {
            this.logger(LogLevel.ERROR, replaceParam(format, arguments), e);
        }
    }

    @Override
    public void error(String format) {
        if (this.isErrorEnable()) {
            this.logger(LogLevel.ERROR, format, null);
        }
    }

    @Override
    public void debug(String format) {
        if (this.isDebugEnable()) {
            this.logger(LogLevel.DEBUG, format, null);
        }
    }

    @Override
    public void debug(String format, Object... arguments) {
        if (this.isDebugEnable()) {
            this.logger(LogLevel.DEBUG, replaceParam(format, arguments), null);
        }
    }

    @Override
    public void debug(Throwable t, String format, Object... arguments) {
        if (this.isDebugEnable()) {
            this.logger(LogLevel.DEBUG, replaceParam(format, arguments), t);
        }
    }

    @Override
    public void trace(String format) {
        if (this.isTraceEnabled()) {
            this.logger(LogLevel.TRACE, format, null);
        }
    }

    @Override
    public void trace(String format, Object... arguments) {
        if (this.isTraceEnabled()) {
            this.logger(LogLevel.TRACE, replaceParam(format, arguments), null);
        }
    }

    @Override
    public void trace(Throwable t, String format, Object... arguments) {
        if (this.isTraceEnabled()) {
            this.logger(LogLevel.TRACE, replaceParam(format, arguments), t);
        }
    }

    @Override
    public boolean isDebugEnable() {
        return LogLevel.DEBUG.compareTo(Config.Logging.LEVEL) >= 0;
    }

    @Override
    public boolean isInfoEnable() {
        return LogLevel.INFO.compareTo(Config.Logging.LEVEL) >= 0;
    }

    @Override
    public boolean isWarnEnable() {
        return LogLevel.WARN.compareTo(Config.Logging.LEVEL) >= 0;
    }

    @Override
    public boolean isErrorEnable() {
        return LogLevel.ERROR.compareTo(Config.Logging.LEVEL) >= 0;
    }

    @Override
    public boolean isTraceEnabled() {
        return LogLevel.TRACE.compareTo(Config.Logging.LEVEL) >= 0;
    }

    protected String replaceParam(String message, Object... parameters) {
        if (message == null) {
            return null;
        }
        int startSize = 0;
        int parametersIndex = 0;
        int index;
        String tmpMessage = message;
        while ((index = message.indexOf("{}", startSize)) != -1) {
            if (parametersIndex >= parameters.length) {
                break;
            }
            /*
              @Fix the Illegal group reference issue
             */
            tmpMessage = tmpMessage.replaceFirst("\\{\\}", Matcher.quoteReplacement(String.valueOf(parameters[parametersIndex++])));
            startSize = index + 2;
        }
        return tmpMessage;
    }

    protected void logger(LogLevel level, String message, Throwable e) {
        WriterFactory.getLogWriter().write(this.format(level, message, e));
    }

    protected abstract String format(LogLevel level, String message, Throwable e);
}
