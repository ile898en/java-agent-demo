package com.mariana.agent.core.boot;

import com.mariana.agent.core.logging.api.ILog;
import com.mariana.agent.core.logging.api.LogManager;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class AgentPackagePath {

    private static final ILog LOGGER = LogManager.getLogger(AgentPackagePath.class);

    private static File AGENT_PACKAGE_PATH;

    public static File getPath() throws AgentPackageNotFoundException {
        if (AGENT_PACKAGE_PATH == null) {
            AGENT_PACKAGE_PATH = findPath();
        }
        return AGENT_PACKAGE_PATH;
    }

    public static boolean isPathFound() {
        return AGENT_PACKAGE_PATH != null;
    }

    private static File findPath() throws AgentPackageNotFoundException {
        String classResourcePath = AgentPackagePath.class.getName()
                .replaceAll("\\.", "/") + ".class";

        URL resource = ClassLoader.getSystemClassLoader().getResource(classResourcePath);
        if (resource != null) {
            String urlString = resource.toString();

            LOGGER.debug("The beacon class location is {}.", urlString);

            int insidePathIndex = urlString.indexOf('!');
            boolean isInJar = insidePathIndex > -1;

            if (isInJar) {
                urlString = urlString.substring(urlString.indexOf("file:"), insidePathIndex);
                File agentJarFile = null;
                try {
                    agentJarFile = new File(new URL(urlString).toURI());
                } catch (MalformedURLException | URISyntaxException e) {
                    LOGGER.error(e, "Can not locate agent jar file by url:" + urlString);
                    throw new AgentPackageNotFoundException("Can not locate agent jar file by url:" + urlString);
                }

                if (agentJarFile.exists()) {
                    return agentJarFile.getParentFile();
                }

            } else {
                int prefixLength = "file:".length();
                String classLocation = urlString.substring(
                        prefixLength, urlString.length() - classResourcePath.length());
                return new File(classLocation);
            }

        }

        LOGGER.error("Can not locate agent jar file.");
        throw new AgentPackageNotFoundException("Can not locate agent jar file.");
    }

}
