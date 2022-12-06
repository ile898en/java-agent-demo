package com.mariana.agent.core.loader;

public class AgentClassLoader extends ClassLoader {

    static {
        // Try to solve the classloader deadlock
        registerAsParallelCapable();
    }

    public AgentClassLoader(ClassLoader parent) {
        super(parent);

    }
}
