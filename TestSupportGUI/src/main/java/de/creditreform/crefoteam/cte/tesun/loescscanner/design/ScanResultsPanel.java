/*
 * Created by JFormDesigner on Tue Feb 11 14:00:13 CET 2025
 */

package de.creditreform.crefoteam.cte.tesun.loescscanner.design;

import de.creditreform.crefoteam.cte.tesun.gui.view.ClosableTabbedPane;
import de.creditreform.crefoteam.cte.tesun.loescscanner.view.ScanResultsTabView;

import javax.swing.*;
import java.awt.*;

/**
 * @author CavdarK
 */
public class ScanResultsPanel extends JPanel {
    public ScanResultsPanel() {
        initComponents();
    }

    public ClosableTabbedPane getTabbedPaneScanResults() {
        return tabbedPaneScanResults;
    }

    public ScanResultsTabView getScanResultsTabView() {
        return scanResultsTabView;
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
      tabbedPaneScanResults = new ClosableTabbedPane();
      scanResultsTabView = new ScanResultsTabView();

      //======== this ========
      setLayout(new GridBagLayout());
      ((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 88, 0};
      ((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0};
      ((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0, 1.0E-4};
      ((GridBagLayout)getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

      //======== tabbedPaneScanResults ========
      {
         tabbedPaneScanResults.addTab("", scanResultsTabView);
      }
      add(tabbedPaneScanResults, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0,
         GridBagConstraints.CENTER, GridBagConstraints.BOTH,
         new Insets(0, 0, 0, 0), 0, 0));
      // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
   private ClosableTabbedPane tabbedPaneScanResults;
   private ScanResultsTabView scanResultsTabView;
   // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
