/*
 * Created by JFormDesigner on Tue Feb 11 13:59:58 CET 2025
 */

package de.creditreform.crefoteam.cte.tesun.loescscanner.design;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

/**
 * @author CavdarK
 */
public class ScanResultsTabPanel extends JPanel {
    public ScanResultsTabPanel() {
        initComponents();
    }

    public JButton getButtonRefreshScanResults() {
        return buttonRefreshScanResults;
    }

    public JButton getButtonLoadScanResults() {
        return buttonLoadScanResults;
    }

    public JButton getButtonSaveScanResults() {
        return buttonSaveScanResults;
    }

    public JComboBox getComboBoxDiffTools() {
        return comboBoxDiffTools;
    }

    public JButton getButtonStartDifTool() {
        return buttonStartDifTool;
    }

    public JSplitPane getSplitPanelTreeView() {
        return splitPanelTreeView;
    }

    public JPanel getPanelScanResults() {
        return panelScanResults;
    }

    public JTree getTreeCustomers() {
        return treeCustomers;
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
      panelControls = new JPanel();
      buttonRefreshScanResults = new JButton();
      buttonLoadScanResults = new JButton();
      buttonSaveScanResults = new JButton();
      label1 = new JLabel();
      comboBoxDiffTools = new JComboBox();
      buttonStartDifTool = new JButton();
      splitPanelTreeView = new JSplitPane();
      panelScanResults = new JPanel();
      scrollPaneScanCustomers = new JScrollPane();
      treeCustomers = new JTree();

      //======== this ========
      setLayout(new BorderLayout());

      //======== panelControls ========
      {
         panelControls.setBorder(new BevelBorder(BevelBorder.LOWERED));
         panelControls.setLayout(new GridBagLayout());
         ((GridBagLayout)panelControls.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
         ((GridBagLayout)panelControls.getLayout()).rowHeights = new int[] {0, 0, 0};
         ((GridBagLayout)panelControls.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0, 1.0E-4};
         ((GridBagLayout)panelControls.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

         //---- buttonRefreshScanResults ----
         buttonRefreshScanResults.setText("Aktualisieren");
         buttonRefreshScanResults.setIcon(new ImageIcon(getClass().getResource("/icons/refresh.png")));
         panelControls.add(buttonRefreshScanResults, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(2, 2, 7, 5), 0, 0));

         //---- buttonLoadScanResults ----
         buttonLoadScanResults.setText("Scan Results Laden...");
         buttonLoadScanResults.setIcon(new ImageIcon(getClass().getResource("/icons/folder_up.png")));
         panelControls.add(buttonLoadScanResults, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(2, 2, 7, 5), 0, 0));

         //---- buttonSaveScanResults ----
         buttonSaveScanResults.setText("Scan Results Speichern");
         buttonSaveScanResults.setIcon(new ImageIcon(getClass().getResource("/icons/save.png")));
         panelControls.add(buttonSaveScanResults, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(2, 2, 7, 0), 0, 0));

         //---- label1 ----
         label1.setText("Diff-Tool:");
         panelControls.add(label1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(1, 2, 2, 7), 0, 0));
         panelControls.add(comboBoxDiffTools, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(1, 2, 2, 7), 0, 0));

         //---- buttonStartDifTool ----
         buttonStartDifTool.setText("Start Diff-Tool");
         panelControls.add(buttonStartDifTool, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(1, 2, 2, 2), 0, 0));
      }
      add(panelControls, BorderLayout.PAGE_START);

      //======== splitPanelTreeView ========
      {
         splitPanelTreeView.setDividerLocation(100);

         //======== panelScanResults ========
         {
            panelScanResults.setLayout(new CardLayout());
         }
         splitPanelTreeView.setRightComponent(panelScanResults);

         //======== scrollPaneScanCustomers ========
         {

            //---- treeCustomers ----
            treeCustomers.setMinimumSize(new Dimension(120, 0));
            scrollPaneScanCustomers.setViewportView(treeCustomers);
         }
         splitPanelTreeView.setLeftComponent(scrollPaneScanCustomers);
      }
      add(splitPanelTreeView, BorderLayout.CENTER);
      // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
   private JPanel panelControls;
   private JButton buttonRefreshScanResults;
   private JButton buttonLoadScanResults;
   private JButton buttonSaveScanResults;
   private JLabel label1;
   private JComboBox comboBoxDiffTools;
   private JButton buttonStartDifTool;
   private JSplitPane splitPanelTreeView;
   private JPanel panelScanResults;
   private JScrollPane scrollPaneScanCustomers;
   private JTree treeCustomers;
   // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
