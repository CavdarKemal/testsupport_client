package de.creditreform.crefoteam.cte.tesun.gui.logsearch;

import de.creditreform.crefoteam.cte.tesun.gui.logsearch.view.SearchLogsView;
import de.creditreform.crefoteam.cte.tesun.gui.utils.GUIFrame;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;

import javax.swing.*;

public class SearchLOGsGUI extends GUIFrame {
    private static final EnvironmentConfig environmentConfig = new EnvironmentConfig("ENE");

    public SearchLOGsGUI() throws Exception {
        super(environmentConfig);
        SearchLogsView searchDefinitionView = new SearchLogsView(this);
        getContentPane().add(searchDefinitionView);
        setVisible(true);
    }

    public static void main(String[] cmdArgs) {
        try {
            //      final String searchCfgFileName;
            //      if( cmdArgs.length < 1 )
            //      {
            //        searchCfgFileName = null;
            //      }
            //      else
            //      {
            //        // ../TestUmgebung/GEE/gee.searchitems.properties
            //        File propsFile = new File( cmdArgs[0] );
            //        if( propsFile.exists() )
            //        {
            //          searchCfgFileName = propsFile.getCanonicalPath();
            //        }
            //        else
            //        {
            //          searchCfgFileName = null;
            //        }
            //      }

            JFrame.setDefaultLookAndFeelDecorated(true);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        new SearchLOGsGUI();
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
