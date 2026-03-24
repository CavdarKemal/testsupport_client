package de.creditreform.crefoteam.cte.tesun.gui.xmlsearch;

import de.creditreform.crefoteam.cte.tesun.gui.utils.GUIFrame;
import de.creditreform.crefoteam.cte.tesun.gui.xmlsearch.view.SearchXMLsView;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;

import javax.swing.*;
import java.io.File;

public class SearchXMLsGUI extends GUIFrame {
    private static final String APP_NAME = "SearchXMLs-GUI";
    private static final String APP_DESCRIPTION = "Suchen in Exportierten XML-Dateien";
    private static final String APP_VERSION = "1.0";
    private static final EnvironmentConfig environmentConfig = new EnvironmentConfig("ENE");

    public SearchXMLsGUI(String searchCfgFileName) throws Exception {
        super(environmentConfig);
        if (searchCfgFileName == null) {
            searchCfgFileName = environmentConfig.getLastCfgFileName();
        }
        SearchXMLsView searchDefinitionView = new SearchXMLsView(this, searchCfgFileName);
        getContentPane().add(searchDefinitionView);
    }

    public static void main(String[] cmdArgs) {
        try {
            final String searchCfgFileName;
            if (cmdArgs.length == 1) {
                // ../TestUmgebung/GEE/gee.searchitems.properties
                File propsFile = new File(cmdArgs[0]);
                if (propsFile.exists()) {
                    searchCfgFileName = propsFile.getCanonicalPath();
                } else {
                    searchCfgFileName = null;
                }
            } else {
                searchCfgFileName = null;
            }

            JFrame.setDefaultLookAndFeelDecorated(true);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        new SearchXMLsGUI(searchCfgFileName);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.exit(-1);
                    }
                }
            });
        } catch (Exception ex) {
            new RuntimeException(ex);
        }
    }
}
