package com.mariana.agent.common.util;

import java.util.concurrent.CompletableFuture;

public class RunnableWithExceptionProtection implements Runnable{

    private final Runnable runnable;
    private final CallbackWhenException callback;

    public RunnableWithExceptionProtection(Runnable runnable, CallbackWhenException callback) {
        this.runnable = runnable;
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            runnable.run();
        } catch (Throwable t) {
            callback.handle(t);
        }
    }

    public interface CallbackWhenException {
        void handle(Throwable t);
    }
}
