package com.mariana.agent.starter;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.jar.JarFile;

public class AgentStarter {

    public static final Class<AgentStarter> TYPE = AgentStarter.class;

    public static void premain(String args, Instrumentation instrumentation) {
        System.out.println("AgentStarter#premain started with args: " + args);
        startAgent(args, instrumentation, true);
    }

    public static void agentmain(String args, Instrumentation instrumentation) {
        System.out.println("AgentStarter#agentmain started with args: " + args);
        startAgent(args, instrumentation, false);
    }

    private synchronized static void startAgent(String args, Instrumentation instrumentation, boolean premain) {

        // checking early as getting a property might not be provided
        securityManagerCheck();

        // workaround for classloader deadlock https://bugs.openjdk.java.net/browse/JDK-8194653
        FileSystems.getDefault();

        try {
            File agentJarFile = getAgentJarFile(args);
            instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(agentJarFile, false));
        } catch (URISyntaxException | IOException e) {
            System.err.println("[simple-agent] ERROR Failed to start agent.");
            e.printStackTrace();
        }


    }


    private static File getAgentJarFile(String args) throws URISyntaxException {

        ProtectionDomain protectionDomain = TYPE.getProtectionDomain();
        CodeSource codeSource = protectionDomain.getCodeSource();
        if (codeSource == null) {
            throw new IllegalStateException(String.format("Unable to get agent location, protection domain = %s", protectionDomain));
        }
        URL location = codeSource.getLocation();
        if (location == null) {
            throw new IllegalStateException(String.format("Unable to get agent location, code source = %s", codeSource));
        }
        final File agentJar = new File(location.toURI());
        if (!agentJar.getName().endsWith(".jar")) {
            throw new IllegalStateException("Agent is not a jar file: " + agentJar);
        }
        return agentJar.getAbsoluteFile();
    }

    private static void securityManagerCheck() {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            return;
        }
        try {
            sm.checkPermission(new AllPermission());
        } catch (SecurityException e) {
            // note: we can't get the actual path of the agent here as the Security Manager might prevent us from finding our own jar.
            System.err.println("[simple-agent] WARN Security manager without agent grant-all permission, adding the following snippet to security policy is recommended:");
            System.err.println("[simple-agent] WARN grant codeBase \"file:/path/to/simple-agent.jar\" {");
            System.err.println("[simple-agent] WARN     permission java.security.AllPermission;");
            System.err.println("[simple-agent] WARN };");
        }
    }

}
