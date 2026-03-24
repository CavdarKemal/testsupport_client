package de.creditreform.crefoteam.cte.tesun.util;

import java.io.File;

/**
 * Manages test environment directories and logging configuration.
 * Creates environment-specific directories under TEST-ENVS and configures
 * logging to use the appropriate log file.
 *
 * @author TemplateGUI
 * @version 1.0
 */
public class TestEnvironmentManager {
    private static EnvironmentConfig currentEnvironment = null;

    private TestEnvironmentManager() {
        // Utility class
    }

    /**
     * Resets the manager state. Useful for testing. Also closes any open FileAppender to release file handles.
     */
    public static void reset() {
        closeLogging();
        EnvironmentLockManager.releaseLock();
        currentEnvironment = null;
    }

    /**
     * Closes all loggers to release file handles. Useful for testing to allow temp directory cleanup.
     */
    public static void closeLogging() {
        TimelineLogger.close();
    }

    /**
     * Switches to a new environment based on the config file name.
     * Creates the necessary directories, acquires environment lock, and reconfigures logging.
     *
     * @param newEnvironmentConfig the EnvironmentConfig
     * @return true if switch was successful, false if environment is locked by another instance
     */
    public static boolean switchEnvironment(EnvironmentConfig newEnvironmentConfig) throws Exception {
        String newEnvName = newEnvironmentConfig.getCurrentEnvName();
        if (currentEnvironment != null && newEnvName.equals(currentEnvironment.getCurrentEnvName())) {
            TimelineLogger.debug(TestEnvironmentManager.class, "Already in environment: {}", newEnvName);
            return true;
        }
        TimelineLogger.info(TestEnvironmentManager.class, "Switching to environment: {}", newEnvName);
        File logsDir = newEnvironmentConfig.getLogOutputsRootForEnv(newEnvName);
        // Check if new environment is locked by another instance
        if (EnvironmentLockManager.isLocked(logsDir)) {
            TimelineLogger.warn(TestEnvironmentManager.class, "Environment {} is locked by another instance", newEnvName);
            return false;
        }
        // Release old lock (if any)
        EnvironmentLockManager.releaseLock();
        // Acquire lock for new environment
        if (!EnvironmentLockManager.acquireLock(logsDir, newEnvName)) {
            TimelineLogger.error(TestEnvironmentManager.class, "Could not acquire lock for environment: {}", newEnvName);
            return false;
        }
        // Update state
        currentEnvironment = newEnvironmentConfig;
        TimelineLogger.info(TestEnvironmentManager.class, "Environment switched to: {} ({})", newEnvName, logsDir.getAbsolutePath());

        return true;
    }

    /**
     * Creates the required directories.
     */
    private static boolean createDirectories(File logsDir, File testOutputsDir) {
        try {
            if (!logsDir.exists() && !logsDir.mkdirs()) {
                TimelineLogger.error(TestEnvironmentManager.class, "Could not create logs directory: {}", logsDir.getAbsolutePath());
                return false;
            }
            if (!testOutputsDir.exists() && !testOutputsDir.mkdirs()) {
                TimelineLogger.error(TestEnvironmentManager.class, "Could not create test outputs directory: {}", testOutputsDir.getAbsolutePath());
                return false;
            }
            TimelineLogger.debug(TestEnvironmentManager.class, "Directories created/verified: {}, {}", logsDir.getAbsolutePath(), testOutputsDir.getAbsolutePath());
            return true;
        } catch (SecurityException e) {
            TimelineLogger.error(TestEnvironmentManager.class, "Security exception creating directories", e);
            return false;
        }
    }

}
