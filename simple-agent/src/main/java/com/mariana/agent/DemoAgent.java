package com.mariana.agent;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.ProtectionDomain;

public class DemoAgent {

    public static final Class<DemoAgent> TYPE = DemoAgent.class;

    private static final Logger logger = LoggerFactory.getLogger(TYPE);

    private static final String JAR_FILE_NAME = "demo-agent.jar";
    private static final String CLASS_FILE_NAME = "com.mariana.agent.DemoAgent";
    private static final String ATTACH_STATUS_KEY = "DemoAgent.attached";

    public static void premain(String args, Instrumentation instrumentation) {
        logger.info("DemoAgent#premain started with args: {}", args);
        init(args, instrumentation, true);
    }

    public static void agentmain(String args, Instrumentation instrumentation) {
        logger.info("DemoAgent#agentmain started with args: {}", args);
        init(args, instrumentation, false);
    }

    private synchronized static void init(String args, Instrumentation instrumentation, boolean premain) {

        // checking early as getting a property might not be provided
        securityManagerCheck();

        if (Boolean.getBoolean(ATTACH_STATUS_KEY)) {
            // agent is already attached; don't attach twice
            // don't fail as this is a valid case
            // for example, Spring Boot restarts the application in dev mode
            return;
        }

        // TODO: add some pre-check logic like 'check jvm version' here

        // workaround for classloader deadlock https://bugs.openjdk.java.net/browse/JDK-8194653
        FileSystems.getDefault();

        try {
            File agentJarFile = getAgentJarFile(args);

        } catch (URISyntaxException e) {
            System.err.println("[demo-agent] ERROR Failed to start agent.");
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
            System.err.println("[demo-agent] WARN Security manager without agent grant-all permission, adding the following snippet to security policy is recommended:");
            System.err.println("[demo-agent] WARN grant codeBase \"file:/path/to/demo-agent.jar\" {");
            System.err.println("[demo-agent] WARN     permission java.security.AllPermission;");
            System.err.println("[demo-agent] WARN };");
        }
    }

}
