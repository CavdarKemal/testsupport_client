package de.creditreform.crefoteam.cte.tesun.gui;

import de.creditreform.crefoteam.cte.tesun.gui.utils.GUIFrame;
import de.creditreform.crefoteam.cte.tesun.gui.view.TestSupportView;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentLockManager;
import de.creditreform.crefoteam.cte.tesun.util.TestEnvironmentManager;
import de.creditreform.crefoteam.cte.tesun.util.TimelineLogger;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.*;
import org.apache.commons.io.FileUtils;

// JVM- Parameter: -Dlog4j.debug=true -Dlog4j.configuration=file:log4j.properties
// Start-Dir: $MODULE_DIR$ (= E:\Projekte\CTE\testsupport_client\TestSupportGUI)

public class TestSupportGUI extends GUIFrame {
    private final static File MAIN_LOG_FILE_NAME = new File(System.getProperty("user.dir"), "TestSuppurt-GUI.log");
    private final TestSupportView testSupportView;

    public TestSupportGUI(EnvironmentConfig environmentConfig) {
        super(environmentConfig);
        try {
            if (!TimelineLogger.configure(environmentConfig.getLogOutputsRoot(), (environmentConfig.getLastTestType() + ".log"), "TimeLine.log")) {
                throw new RuntimeException("Exception beim Konfigurieren der LOG-Dateien!\n");
            }
            if (!TestEnvironmentManager.switchEnvironment(environmentConfig)) {
                throw new RuntimeException("Die Umgebung " + environmentConfig.getCurrentEnvName() + " ist gesperrt, da eine andere Instanz in dieser Umgebung läuft.");
            }
            //  Shutdown Hook registrieren
            EnvironmentLockManager.registerShutdownHook();

            this.testSupportView = new TestSupportView(this);
            getContentPane().add(testSupportView);
            setVisible(true);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static EnvironmentConfig handleArguments(String[] cmdArgs) {
        String envName = null;
        String testSource = null;
        String itsqRevision = null;
        for (String cmdArg : cmdArgs) {
            String[] split = cmdArg.split("[:=]");
            if (split.length < 2) {
                String strError = String.format("\n\tDer Parameter '%s' wurde falsch angegeben!\n\tZulässige Parameter: -e:<Umgebung> -r:<Test-Root> -b:<Branch>", cmdArg);
                throw new IllegalStateException(strError);
            }
            String argName = split[0];
            String argValue = split[1];
            if (argName.equals("-e") || argName.equals("-e")) {
                envName = argValue;
            } else {
                String strError = String.format("\n\tDer Parameter '%s' wird nicht unterstützt!\n\tZulässige Parameter: -e:<Umgebung> -r:<Test-Root> -b:<Branch>", cmdArg);
                throw new IllegalStateException(strError);
            }
        }
        if (envName == null) {
            envName = "ENE";
        }
        EnvironmentConfig environmentConfig = new EnvironmentConfig(envName);
        return environmentConfig;
    }

    public static void main(String[] cmdArgs) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        SwingUtilities.invokeLater(() -> {
            try {
                new TestSupportGUI(handleArguments(cmdArgs));
            } catch (Exception ex) {
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                ex.printStackTrace(printWriter);
                String exMsg = ex.getMessage();
                if (exMsg == null && ex.getCause() != null) {
                    exMsg = ex.getCause().getMessage();
                }
                String errMsg = exMsg + "\n" + stringWriter;
                JOptionPane.showMessageDialog(null, "Fehler beim Starten der GUI!\n" + errMsg, "TestSupport-GUI", JOptionPane.CANCEL_OPTION);
                try {
                    FileUtils.write(MAIN_LOG_FILE_NAME, errMsg, true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    System.exit(-1);
                }
            }
        });
    }

}
