package com.mariana.agent.core.logging.core;

import com.google.common.base.Strings;

public class PatternLogger extends AbstractLogger{

    public static final String DEFAULT_PATTERN = "%level %timestamp %thread %class : %msg %throwable";

    private String pattern;

    public PatternLogger(Class<?> targetClass, String pattern) {
        this(targetClass.getSimpleName(), pattern);
    }

    public PatternLogger(String targetClass, String pattern) {
        super(targetClass);
        this.setPattern(pattern);
    }

    public PatternLogger(String targetClass) {
        super(targetClass);
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        if (Strings.isNullOrEmpty(pattern)) {
            pattern = DEFAULT_PATTERN;
        }
        this.pattern = pattern;
        this.converters = new Parser(pattern, DEFAULT_CONVERTER_MAP).parse();
    }

    @Override
    protected String format(LogLevel level, String message, Throwable e) {
        LogEvent logEvent = new LogEvent(level, message, e, targetClass);
        StringBuilder stringBuilder = new StringBuilder();
        for (Converter converter : this.converters) {
            stringBuilder.append(converter.convert(logEvent));
        }
        return stringBuilder.toString();
    }
}
