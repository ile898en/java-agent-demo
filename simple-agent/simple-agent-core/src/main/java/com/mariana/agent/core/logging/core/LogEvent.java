package com.mariana.agent.core.logging.core;

public class LogEvent {

    private LogLevel level;
    private String message;
    private Throwable throwable;
    private String targetClass;

    public LogEvent(LogLevel level, String message, Throwable throwable, String targetClass) {
        this.level = level;
        this.message = message;
        this.throwable = throwable;
        this.targetClass = targetClass;
    }

    public LogLevel getLevel() {
        return level;
    }

    public void setLevel(LogLevel level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public String getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(String targetClass) {
        this.targetClass = targetClass;
    }
}
