/*
 * Created by JFormDesigner on Mon Jan 25 16:35:02 CET 2016
 */

package de.creditreform.crefoteam.cte.tesun.gui.design;

import de.creditreform.crefoteam.cte.tesun.gui.view.CustomersSelectionView;
import de.creditreform.crefoteam.cte.tesun.gui.view.TestResultsView;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;

/**
 * @author Uzayli Temel
 */
public class TestSupportPanel extends JPanel {
    public TestSupportPanel() {
        initComponents();
    }

    public JLabel getLabelEnvironment() {
        return labelEnvironment;
    }

    public JComboBox getComboBoxEnvironment() {
        return comboBoxEnvironment;
    }

    public JButton getButtonStartProcess() {
        return buttonStartProcess;
    }

    public JSplitPane getSplitPaneMain() {
        return splitPaneMain;
    }

    public CustomersSelectionView getViewcustomersSelection() {
        return viewcustomersSelection;
    }

    public JPanel getPanelRight() {
        return panelRight;
    }

    public JScrollPane getScrollPaneTaskListenerInfo() {
        return scrollPaneTaskListenerInfo;
    }

    public JTextArea getTextAreaTaskListenerInfo() {
        return textAreaTaskListenerInfo;
    }

    public JPanel getPanelLeft() {
        return panelLeft;
    }

    public JButton getButtonStopUserTasksThread() {
        return buttonStopUserTasksThread;
    }

    public JComboBox getComboBoxTestType() {
        return comboBoxTestType;
    }

    public JLabel getLabelTestCasesPath() {
        return labelTestCasesPath;
    }

    public JTextField getTextFieldTestCasesPath() {
        return textFieldTestCasesPath;
    }

    public JPanel getPanelControls() {
        return panelControls;
    }

    public JLabel getLabelActivitiHost() {
        return labelActivitiHost;
    }

    public JLabel getLabelRestServicesHost() {
        return labelRestServicesHost;
    }

    public JButton getButtonRefreshEnvironment() {
        return buttonRefreshEnvironment;
    }

    public JLabel getLabelTestJobs() {
        return labelTestJobs;
    }

    public JComboBox getComboBoxTestJobs() {
        return comboBoxTestJobs;
    }

    public JButton getButtonStartTestJob() {
        return buttonStartTestJob;
    }

    public JTabbedPane getTabbedPaneMonitor() {
        return tabbedPaneMonitor;
    }

    public JPanel getPanelLogs() {
        return panelLogs;
    }

    public JButton getButtonClearLOGPanel() {
        return buttonClearLOGPanel;
    }

    public JScrollPane getScrollPanelProcessImage() {
        return scrollPanelProcessImage;
    }

    public JLabel getLabelProcessImage() {
        return labelProcessImage;
    }

    public JCheckBox getCheckBoxScrollToEnd() {
        return checkBoxScrollToEnd;
    }

    public JButton getButtonManageJVMs() {
        return buttonManageJVMs;
    }

    public JLabel getLabelJobParams() {
        return labelJobParams;
    }

    public JTextField getTextFieldJobParams() {
        return textFieldJobParams;
    }

    public JComboBox getComboBoxTestSource() {
        return comboBoxTestSource;
    }

    public JLabel getLabelTestType3() {
        return labelTestType3;
    }

    public JLabel getLabelTestTestSource() {
        return labelTestTestSource;
    }

    public JSeparator getSeparator1() {
        return separator1;
    }

    public JLabel getLabelITSQRevision() {
        return labelITSQRevision;
    }

    public JComboBox getComboBoxITSQRevision() {
        return comboBoxITSQRevision;
    }

    public JLabel getLabelFachwertConfig() {
        return labelFachwertConfig;
    }

    public JLabel getLabelExportFormat() {
        return labelExportFormat;
    }

    public JRadioButton getRadioButtonFWConfigNewest() {
        return radioButtonFWConfigNewest;
    }

    public JRadioButton getRadioButtonFWConfigLikePRE() {
        return radioButtonFWConfigLikePRE;
    }

    public JRadioButton getRadioButtonExportFormatNewest() {
        return radioButtonExportFormatNewest;
    }

    public JRadioButton getRadioButtonExportFormatLikePRE() {
        return radioButtonExportFormatLikePRE;
    }

    public JCheckBox getCheckBoxDemoMode() {
        return checkBoxDemoMode;
    }

    public JComboBox getComboBoxRestServicesHost() {
        return comboBoxRestServicesHost;
    }

    public JComboBox getComboBoxActivitiHost() {
        return comboBoxActivitiHost;
    }

    public TestResultsView getViewTestResults() {
        return viewTestResults;
    }

    public JCheckBox getCheckBoxUseOnlyTestCLZs() {
        return checkBoxUseOnlyTestCLZs;
    }

    public JLabel getLabelBatchGUIHost() {
        return labelBatchGUIHost;
    }

    public JComboBox getComboBoxBatchGUIHost() {
        return comboBoxBatchGUIHost;
    }

    public JLabel getLabelImpCycleHost() {
        return labelImpCycleHost;
    }

    public JComboBox getComboBoxImpCycleHost() {
        return comboBoxImpCycleHost;
    }

    public JLabel getLabelInsoHost() {
        return labelInsoHost;
    }

    public JComboBox getComboBoxInsoHost() {
        return comboBoxInsoHost;
    }

    public JLabel getLabelInsoBackEndHost() {
        return labelInsoBackEndHost;
    }

    public JComboBox getComboBoxInsoBackEndHost() {
        return comboBoxInsoBackEndHost;
    }

    public JLabel getLabelTestPhase() {
        return labelTestPhase;
    }

    public JComboBox getComboBoxTestPhase() {
        return comboBoxTestPhase;
    }

    public JCheckBox getCheckBoxUploadSynthetics() {
       return checkBoxUploadSynthetics;
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        splitPaneMain = new JSplitPane();
        panelLeft = new JPanel();
        panelControls = new JPanel();
        labelEnvironment = new JLabel();
        comboBoxEnvironment = new JComboBox();
        buttonRefreshEnvironment = new JButton();
        buttonManageJVMs = new JButton();
        labelActivitiHost = new JLabel();
        comboBoxActivitiHost = new JComboBox();
        labelRestServicesHost = new JLabel();
        comboBoxRestServicesHost = new JComboBox();
        labelBatchGUIHost = new JLabel();
        comboBoxBatchGUIHost = new JComboBox();
        labelImpCycleHost = new JLabel();
        comboBoxImpCycleHost = new JComboBox();
        labelInsoHost = new JLabel();
        comboBoxInsoHost = new JComboBox();
        labelInsoBackEndHost = new JLabel();
        comboBoxInsoBackEndHost = new JComboBox();
        viewcustomersSelection = new CustomersSelectionView();
        panelRight = new JPanel();
        labelTestType3 = new JLabel();
        comboBoxTestType = new JComboBox();
        labelTestTestSource = new JLabel();
        comboBoxTestSource = new JComboBox();
        labelITSQRevision = new JLabel();
        comboBoxITSQRevision = new JComboBox();
        checkBoxDemoMode = new JCheckBox();
        buttonStartProcess = new JButton();
        buttonStopUserTasksThread = new JButton();
        labelTestCasesPath = new JLabel();
        textFieldTestCasesPath = new JTextField();
        checkBoxUploadSynthetics = new JCheckBox();
        checkBoxUseOnlyTestCLZs = new JCheckBox();
        labelFachwertConfig = new JLabel();
        radioButtonFWConfigNewest = new JRadioButton();
        radioButtonFWConfigLikePRE = new JRadioButton();
        labelExportFormat = new JLabel();
        radioButtonExportFormatNewest = new JRadioButton();
        radioButtonExportFormatLikePRE = new JRadioButton();
        separator1 = new JSeparator();
        labelTestJobs = new JLabel();
        comboBoxTestJobs = new JComboBox();
        labelTestPhase = new JLabel();
        comboBoxTestPhase = new JComboBox();
        buttonStartTestJob = new JButton();
        labelJobParams = new JLabel();
        textFieldJobParams = new JTextField();
        tabbedPaneMonitor = new JTabbedPane();
        panelLogs = new JPanel();
        scrollPaneTaskListenerInfo = new JScrollPane();
        textAreaTaskListenerInfo = new JTextArea();
        checkBoxScrollToEnd = new JCheckBox();
        buttonClearLOGPanel = new JButton();
        scrollPanelProcessImage = new JScrollPane();
        labelProcessImage = new JLabel();
        viewTestResults = new TestResultsView();

        //======== this ========
        setBorder(new EtchedBorder());
        setName("this");
        setLayout(new GridBagLayout());
        ((GridBagLayout)getLayout()).columnWidths = new int[] {0, 74, 0, 135, 367, 0};
        ((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0};
        ((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0, 1.0, 1.0, 1.0E-4};
        ((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

        //======== splitPaneMain ========
        {
           splitPaneMain.setDividerLocation(340);
           splitPaneMain.setDividerSize(5);
           splitPaneMain.setName("splitPaneMain");

           //======== panelLeft ========
           {
              panelLeft.setName("panelLeft");
              panelLeft.setLayout(new BorderLayout());

              //======== panelControls ========
              {
                 panelControls.setName("panelControls");
                 panelControls.setLayout(new GridBagLayout());
                 ((GridBagLayout)panelControls.getLayout()).columnWidths = new int[] {0, 81, 14, 0};
                 ((GridBagLayout)panelControls.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0};
                 ((GridBagLayout)panelControls.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0, 1.0E-4};
                 ((GridBagLayout)panelControls.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

                 //---- labelEnvironment ----
                 labelEnvironment.setText("Umgebung:");
                 labelEnvironment.setName("labelEnvironment");
                 panelControls.add(labelEnvironment, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(2, 2, 6, 7), 0, 0));

                 //---- comboBoxEnvironment ----
                 comboBoxEnvironment.setName("comboBoxEnvironment");
                 panelControls.add(comboBoxEnvironment, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(2, 2, 6, 7), 0, 0));

                 //---- buttonRefreshEnvironment ----
                 buttonRefreshEnvironment.setIcon(new ImageIcon(getClass().getResource("/icons/folder_refresh.png")));
                 buttonRefreshEnvironment.setActionCommand("UT-Thread starten...");
                 buttonRefreshEnvironment.setToolTipText("UT-Thread starten...");
                 buttonRefreshEnvironment.setText("Aktualisieren");
                 buttonRefreshEnvironment.setMinimumSize(new Dimension(24, 24));
                 buttonRefreshEnvironment.setPreferredSize(new Dimension(24, 24));
                 buttonRefreshEnvironment.setName("buttonRefreshEnvironment");
                 panelControls.add(buttonRefreshEnvironment, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(2, 2, 6, 2), 0, 0));

                 //---- buttonManageJVMs ----
                 buttonManageJVMs.setIcon(new ImageIcon(getClass().getResource("/icons/folder_out.png")));
                 buttonManageJVMs.setActionCommand("UT-Thread starten...");
                 buttonManageJVMs.setToolTipText("JVM's aktualisieren");
                 buttonManageJVMs.setText("JVM's...");
                 buttonManageJVMs.setName("buttonManageJVMs");
                 panelControls.add(buttonManageJVMs, new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(1, 1, 6, 1), 0, 0));

                 //---- labelActivitiHost ----
                 labelActivitiHost.setText("Activiti-Host:");
                 labelActivitiHost.setName("labelActivitiHost");
                 panelControls.add(labelActivitiHost, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 1, 5, 6), 0, 0));

                 //---- comboBoxActivitiHost ----
                 comboBoxActivitiHost.setName("comboBoxActivitiHost");
                 panelControls.add(comboBoxActivitiHost, new GridBagConstraints(1, 2, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 1, 5, 1), 0, 0));

                 //---- labelRestServicesHost ----
                 labelRestServicesHost.setText("RestServices-Host:");
                 labelRestServicesHost.setName("labelRestServicesHost");
                 panelControls.add(labelRestServicesHost, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 1, 5, 6), 0, 0));

                 //---- comboBoxRestServicesHost ----
                 comboBoxRestServicesHost.setName("comboBoxRestServicesHost");
                 panelControls.add(comboBoxRestServicesHost, new GridBagConstraints(1, 3, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 1, 5, 1), 0, 0));

                 //---- labelBatchGUIHost ----
                 labelBatchGUIHost.setText("Batch-GUI-Host:");
                 labelBatchGUIHost.setName("labelBatchGUIHost");
                 panelControls.add(labelBatchGUIHost, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 1, 5, 6), 0, 0));

                 //---- comboBoxBatchGUIHost ----
                 comboBoxBatchGUIHost.setName("comboBoxBatchGUIHost");
                 panelControls.add(comboBoxBatchGUIHost, new GridBagConstraints(1, 4, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 1, 5, 1), 0, 0));

                 //---- labelImpCycleHost ----
                 labelImpCycleHost.setText("ImportCycle-Host:");
                 labelImpCycleHost.setName("labelImpCycleHost");
                 panelControls.add(labelImpCycleHost, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 1, 5, 6), 0, 0));

                 //---- comboBoxImpCycleHost ----
                 comboBoxImpCycleHost.setName("comboBoxImpCycleHost");
                 panelControls.add(comboBoxImpCycleHost, new GridBagConstraints(1, 5, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 1, 5, 1), 0, 0));

                 //---- labelInsoHost ----
                 labelInsoHost.setText("INSO-Host:");
                 labelInsoHost.setName("labelInsoHost");
                 panelControls.add(labelInsoHost, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 1, 5, 6), 0, 0));

                 //---- comboBoxInsoHost ----
                 comboBoxInsoHost.setName("comboBoxInsoHost");
                 panelControls.add(comboBoxInsoHost, new GridBagConstraints(1, 6, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 1, 5, 1), 0, 0));

                 //---- labelInsoBackEndHost ----
                 labelInsoBackEndHost.setText("INSO-Backend-Host:");
                 labelInsoBackEndHost.setName("labelInsoBackEndHost");
                 panelControls.add(labelInsoBackEndHost, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 1, 0, 6), 0, 0));

                 //---- comboBoxInsoBackEndHost ----
                 comboBoxInsoBackEndHost.setName("comboBoxInsoBackEndHost");
                 panelControls.add(comboBoxInsoBackEndHost, new GridBagConstraints(1, 7, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 1, 0, 1), 0, 0));
              }
              panelLeft.add(panelControls, BorderLayout.NORTH);

              //---- viewcustomersSelection ----
              viewcustomersSelection.setBorder(new BevelBorder(BevelBorder.LOWERED));
              viewcustomersSelection.setName("viewcustomersSelection");
              panelLeft.add(viewcustomersSelection, BorderLayout.CENTER);
           }
           splitPaneMain.setLeftComponent(panelLeft);

           //======== panelRight ========
           {
              panelRight.setBorder(null);
              panelRight.setName("panelRight");
              panelRight.setLayout(new GridBagLayout());
              ((GridBagLayout)panelRight.getLayout()).columnWidths = new int[] {51, 24, 37, 54, 30, 48, 50, 51, 68, 42, 77, 111, 0, 0};
              ((GridBagLayout)panelRight.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
              ((GridBagLayout)panelRight.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 1.0E-4};
              ((GridBagLayout)panelRight.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0E-4};

              //---- labelTestType3 ----
              labelTestType3.setText("Test-Typ:");
              labelTestType3.setName("labelTestType3");
              panelRight.add(labelTestType3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                 new Insets(4, 1, 6, 6), 0, 0));

              //---- comboBoxTestType ----
              comboBoxTestType.setName("comboBoxTestType");
              panelRight.add(comboBoxTestType, new GridBagConstraints(1, 0, 3, 1, 0.0, 0.0,
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                 new Insets(4, 1, 6, 6), 0, 0));

              //---- labelTestTestSource ----
              labelTestTestSource.setText("Test-Source:");
              labelTestTestSource.setName("labelTestTestSource");
              panelRight.add(labelTestTestSource, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
                 GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
                 new Insets(4, 1, 6, 6), 0, 0));

              //---- comboBoxTestSource ----
              comboBoxTestSource.setName("comboBoxTestSource");
              panelRight.add(comboBoxTestSource, new GridBagConstraints(5, 0, 2, 1, 0.0, 0.0,
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                 new Insets(4, 1, 6, 6), 0, 0));

              //---- labelITSQRevision ----
              labelITSQRevision.setText("Branch:");
              labelITSQRevision.setName("labelITSQRevision");
              panelRight.add(labelITSQRevision, new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0,
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                 new Insets(4, 1, 6, 6), 0, 0));

              //---- comboBoxITSQRevision ----
              comboBoxITSQRevision.setName("comboBoxITSQRevision");
              panelRight.add(comboBoxITSQRevision, new GridBagConstraints(8, 0, 1, 1, 0.0, 0.0,
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                 new Insets(4, 1, 6, 6), 0, 0));

              //---- checkBoxDemoMode ----
              checkBoxDemoMode.setText("Demo");
              checkBoxDemoMode.setName("checkBoxDemoMode");
              panelRight.add(checkBoxDemoMode, new GridBagConstraints(9, 0, 1, 1, 0.0, 0.0,
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                 new Insets(0, 0, 5, 5), 0, 0));

              //---- buttonStartProcess ----
              buttonStartProcess.setText("Prozess starten...");
              buttonStartProcess.setIcon(new ImageIcon(getClass().getResource("/icons/gear_run.png")));
              buttonStartProcess.setMinimumSize(new Dimension(80, 24));
              buttonStartProcess.setMaximumSize(new Dimension(120, 24));
              buttonStartProcess.setPreferredSize(new Dimension(80, 24));
              buttonStartProcess.setName("buttonStartProcess");
              panelRight.add(buttonStartProcess, new GridBagConstraints(10, 0, 2, 1, 0.0, 0.0,
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                 new Insets(2, 1, 6, 6), 0, 0));

              //---- buttonStopUserTasksThread ----
              buttonStopUserTasksThread.setIcon(new ImageIcon(getClass().getResource("/icons/cancel.png")));
              buttonStopUserTasksThread.setActionCommand("UT-Thread starten...");
              buttonStopUserTasksThread.setToolTipText("UT-Thread starten...");
              buttonStopUserTasksThread.setMinimumSize(new Dimension(24, 24));
              buttonStopUserTasksThread.setMaximumSize(new Dimension(24, 24));
              buttonStopUserTasksThread.setPreferredSize(new Dimension(24, 24));
              buttonStopUserTasksThread.setName("buttonStopUserTasksThread");
              panelRight.add(buttonStopUserTasksThread, new GridBagConstraints(12, 0, 1, 1, 0.0, 0.0,
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                 new Insets(2, 1, 6, 1), 0, 0));

              //---- labelTestCasesPath ----
              labelTestCasesPath.setText("Testf\u00e4lle:");
              labelTestCasesPath.setName("labelTestCasesPath");
              panelRight.add(labelTestCasesPath, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                 new Insets(2, 1, 6, 6), 0, 0));

              //---- textFieldTestCasesPath ----
              textFieldTestCasesPath.setEditable(false);
              textFieldTestCasesPath.setName("textFieldTestCasesPath");
              panelRight.add(textFieldTestCasesPath, new GridBagConstraints(1, 1, 9, 1, 0.0, 0.0,
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                 new Insets(2, 1, 6, 6), 0, 0));

              //---- checkBoxUploadSynthetics ----
              checkBoxUploadSynthetics.setText("Synthetische Tests hochladen");
              checkBoxUploadSynthetics.setSelected(true);
              checkBoxUploadSynthetics.setName("checkBoxUploadSynthetics");
              panelRight.add(checkBoxUploadSynthetics, new GridBagConstraints(10, 1, 1, 1, 0.0, 0.0,
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                 new Insets(2, 2, 7, 7), 0, 0));

              //---- checkBoxUseOnlyTestCLZs ----
              checkBoxUseOnlyTestCLZs.setText("Nur Test-CLZs in VC-Liste");
              checkBoxUseOnlyTestCLZs.setSelected(true);
              checkBoxUseOnlyTestCLZs.setName("checkBoxUseOnlyTestCLZs");
              panelRight.add(checkBoxUseOnlyTestCLZs, new GridBagConstraints(11, 1, 1, 1, 0.0, 0.0,
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                 new Insets(2, 2, 7, 7), 0, 0));

              //---- labelFachwertConfig ----
              labelFachwertConfig.setText("FW-Konfiguration:");
              labelFachwertConfig.setName("labelFachwertConfig");
              panelRight.add(labelFachwertConfig, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                 new Insets(2, 1, 6, 6), 0, 0));

              //---- radioButtonFWConfigNewest ----
              radioButtonFWConfigNewest.setText("Neu");
              radioButtonFWConfigNewest.setName("radioButtonFWConfigNewest");
              panelRight.add(radioButtonFWConfigNewest, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                 new Insets(2, 1, 6, 6), 0, 0));

              //---- radioButtonFWConfigLikePRE ----
              radioButtonFWConfigLikePRE.setText("PRE");
              radioButtonFWConfigLikePRE.setName("radioButtonFWConfigLikePRE");
              panelRight.add(radioButtonFWConfigLikePRE, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                 new Insets(2, 1, 6, 6), 0, 0));

              //---- labelExportFormat ----
              labelExportFormat.setText("Export-Format:");
              labelExportFormat.setName("labelExportFormat");
              panelRight.add(labelExportFormat, new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0,
                 GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
                 new Insets(2, 1, 6, 6), 0, 0));

              //---- radioButtonExportFormatNewest ----
              radioButtonExportFormatNewest.setText("Neu");
              radioButtonExportFormatNewest.setName("radioButtonExportFormatNewest");
              panelRight.add(radioButtonExportFormatNewest, new GridBagConstraints(5, 2, 1, 1, 0.0, 0.0,
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                 new Insets(2, 1, 6, 6), 0, 0));

              //---- radioButtonExportFormatLikePRE ----
              radioButtonExportFormatLikePRE.setText("PRE");
              radioButtonExportFormatLikePRE.setName("radioButtonExportFormatLikePRE");
              panelRight.add(radioButtonExportFormatLikePRE, new GridBagConstraints(6, 2, 1, 1, 0.0, 0.0,
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                 new Insets(2, 1, 6, 6), 0, 0));

              //---- separator1 ----
              separator1.setName("separator1");
              panelRight.add(separator1, new GridBagConstraints(0, 3, 13, 1, 0.0, 0.0,
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                 new Insets(2, 1, 6, 1), 0, 0));

              //---- labelTestJobs ----
              labelTestJobs.setText("Test-Jobs:");
              labelTestJobs.setName("labelTestJobs");
              panelRight.add(labelTestJobs, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                 new Insets(2, 1, 6, 6), 0, 0));

              //---- comboBoxTestJobs ----
              comboBoxTestJobs.setName("comboBoxTestJobs");
              panelRight.add(comboBoxTestJobs, new GridBagConstraints(1, 4, 6, 1, 0.0, 0.0,
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                 new Insets(2, 1, 6, 6), 0, 0));

              //---- labelTestPhase ----
              labelTestPhase.setText("Test-Phase:");
              labelTestPhase.setName("labelTestPhase");
              panelRight.add(labelTestPhase, new GridBagConstraints(9, 4, 1, 1, 0.0, 0.0,
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                 new Insets(4, 1, 6, 6), 0, 0));

              //---- comboBoxTestPhase ----
              comboBoxTestPhase.setName("comboBoxTestPhase");
              panelRight.add(comboBoxTestPhase, new GridBagConstraints(10, 4, 1, 1, 0.0, 0.0,
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                 new Insets(4, 1, 6, 6), 0, 0));

              //---- buttonStartTestJob ----
              buttonStartTestJob.setText("JOB starten...");
              buttonStartTestJob.setIcon(new ImageIcon(getClass().getResource("/icons/gear_run.png")));
              buttonStartTestJob.setPreferredSize(new Dimension(58, 24));
              buttonStartTestJob.setMinimumSize(new Dimension(18, 24));
              buttonStartTestJob.setMaximumSize(new Dimension(88, 24));
              buttonStartTestJob.setName("buttonStartTestJob");
              panelRight.add(buttonStartTestJob, new GridBagConstraints(11, 4, 2, 1, 0.0, 0.0,
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                 new Insets(2, 1, 6, 1), 0, 0));

              //---- labelJobParams ----
              labelJobParams.setText("Job-Params:");
              labelJobParams.setFocusable(false);
              labelJobParams.setName("labelJobParams");
              panelRight.add(labelJobParams, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                 new Insets(2, 1, 6, 6), 0, 0));

              //---- textFieldJobParams ----
              textFieldJobParams.setName("textFieldJobParams");
              panelRight.add(textFieldJobParams, new GridBagConstraints(1, 5, 10, 1, 0.0, 0.0,
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                 new Insets(2, 1, 6, 6), 0, 0));

              //======== tabbedPaneMonitor ========
              {
                 tabbedPaneMonitor.setName("tabbedPaneMonitor");

                 //======== panelLogs ========
                 {
                    panelLogs.setName("panelLogs");
                    panelLogs.setLayout(new GridBagLayout());
                    ((GridBagLayout)panelLogs.getLayout()).columnWidths = new int[] {0, 0, 0};
                    ((GridBagLayout)panelLogs.getLayout()).rowHeights = new int[] {0, 0, 0};
                    ((GridBagLayout)panelLogs.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
                    ((GridBagLayout)panelLogs.getLayout()).rowWeights = new double[] {1.0, 0.0, 1.0E-4};

                    //======== scrollPaneTaskListenerInfo ========
                    {
                       scrollPaneTaskListenerInfo.setName("scrollPaneTaskListenerInfo");

                       //---- textAreaTaskListenerInfo ----
                       textAreaTaskListenerInfo.setTabSize(4);
                       textAreaTaskListenerInfo.setLineWrap(true);
                       textAreaTaskListenerInfo.setEditable(false);
                       textAreaTaskListenerInfo.setBackground(Color.white);
                       textAreaTaskListenerInfo.setFont(new Font("Verdana", Font.PLAIN, 12));
                       textAreaTaskListenerInfo.setMinimumSize(new Dimension(455, 16));
                       textAreaTaskListenerInfo.setName("textAreaTaskListenerInfo");
                       scrollPaneTaskListenerInfo.setViewportView(textAreaTaskListenerInfo);
                    }
                    panelLogs.add(scrollPaneTaskListenerInfo, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
                       GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                       new Insets(0, 0, 0, 0), 0, 0));

                    //---- checkBoxScrollToEnd ----
                    checkBoxScrollToEnd.setText("Auto-Scroll");
                    checkBoxScrollToEnd.setName("checkBoxScrollToEnd");
                    panelLogs.add(checkBoxScrollToEnd, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                       GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                       new Insets(2, 2, 2, 2), 0, 0));

                    //---- buttonClearLOGPanel ----
                    buttonClearLOGPanel.setIcon(new ImageIcon(getClass().getResource("/icons/table_replace.png")));
                    buttonClearLOGPanel.setActionCommand("LOG-Panel leeren");
                    buttonClearLOGPanel.setToolTipText("LOG-Panel leeren");
                    buttonClearLOGPanel.setText("LOG-Panel leeren");
                    buttonClearLOGPanel.setName("buttonClearLOGPanel");
                    panelLogs.add(buttonClearLOGPanel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                       GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                       new Insets(2, 2, 2, 2), 0, 0));
                 }
                 tabbedPaneMonitor.addTab("Logs", panelLogs);

                 //======== scrollPanelProcessImage ========
                 {
                    scrollPanelProcessImage.setName("scrollPanelProcessImage");

                    //---- labelProcessImage ----
                    labelProcessImage.setName("labelProcessImage");
                    scrollPanelProcessImage.setViewportView(labelProcessImage);
                 }
                 tabbedPaneMonitor.addTab("Prozess-Image", scrollPanelProcessImage);

                 //---- viewTestResults ----
                 viewTestResults.setName("viewTestResults");
                 tabbedPaneMonitor.addTab("Test-Results", viewTestResults);
              }
              panelRight.add(tabbedPaneMonitor, new GridBagConstraints(0, 6, 13, 3, 0.0, 0.0,
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                 new Insets(0, 0, 5, 0), 0, 0));
           }
           splitPaneMain.setRightComponent(panelRight);
        }
        add(splitPaneMain, new GridBagConstraints(0, 0, 5, 2, 0.0, 0.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(0, 0, 0, 0), 0, 0));

        //---- buttonGroup1 ----
        var buttonGroup1 = new ButtonGroup();
        buttonGroup1.add(radioButtonFWConfigNewest);
        buttonGroup1.add(radioButtonFWConfigLikePRE);

        //---- buttonGroup2 ----
        var buttonGroup2 = new ButtonGroup();
        buttonGroup2.add(radioButtonExportFormatNewest);
        buttonGroup2.add(radioButtonExportFormatLikePRE);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JSplitPane splitPaneMain;
    private JPanel panelLeft;
    private JPanel panelControls;
    private JLabel labelEnvironment;
    private JComboBox comboBoxEnvironment;
    private JButton buttonRefreshEnvironment;
    private JButton buttonManageJVMs;
    private JLabel labelActivitiHost;
    private JComboBox comboBoxActivitiHost;
    private JLabel labelRestServicesHost;
    private JComboBox comboBoxRestServicesHost;
    private JLabel labelBatchGUIHost;
    private JComboBox comboBoxBatchGUIHost;
    private JLabel labelImpCycleHost;
    private JComboBox comboBoxImpCycleHost;
    private JLabel labelInsoHost;
    private JComboBox comboBoxInsoHost;
    private JLabel labelInsoBackEndHost;
    private JComboBox comboBoxInsoBackEndHost;
    private CustomersSelectionView viewcustomersSelection;
    private JPanel panelRight;
    private JLabel labelTestType3;
    private JComboBox comboBoxTestType;
    private JLabel labelTestTestSource;
    private JComboBox comboBoxTestSource;
    private JLabel labelITSQRevision;
    private JComboBox comboBoxITSQRevision;
    private JCheckBox checkBoxDemoMode;
    private JButton buttonStartProcess;
    private JButton buttonStopUserTasksThread;
    private JLabel labelTestCasesPath;
    private JTextField textFieldTestCasesPath;
    private JCheckBox checkBoxUploadSynthetics;
    private JCheckBox checkBoxUseOnlyTestCLZs;
    private JLabel labelFachwertConfig;
    private JRadioButton radioButtonFWConfigNewest;
    private JRadioButton radioButtonFWConfigLikePRE;
    private JLabel labelExportFormat;
    private JRadioButton radioButtonExportFormatNewest;
    private JRadioButton radioButtonExportFormatLikePRE;
    private JSeparator separator1;
    private JLabel labelTestJobs;
    private JComboBox comboBoxTestJobs;
    private JLabel labelTestPhase;
    private JComboBox comboBoxTestPhase;
    private JButton buttonStartTestJob;
    private JLabel labelJobParams;
    private JTextField textFieldJobParams;
    private JTabbedPane tabbedPaneMonitor;
    private JPanel panelLogs;
    private JScrollPane scrollPaneTaskListenerInfo;
    private JTextArea textAreaTaskListenerInfo;
    private JCheckBox checkBoxScrollToEnd;
    private JButton buttonClearLOGPanel;
    private JScrollPane scrollPanelProcessImage;
    private JLabel labelProcessImage;
    private TestResultsView viewTestResults;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
