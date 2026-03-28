/*
 * Created by JFormDesigner on Sat Mar 28 14:13:03 CET 2026
 */

package de.creditreform.crefoteam.cte.tesun.gui.design;

import java.awt.*;
import java.util.*;
import javax.swing.*;

/**
 * @author CavdarK
 */
public class TestSupportMainProcessPanel extends JPanel {
    public TestSupportMainProcessPanel() {
        initComponents();
    }

    public JLabel getLabelTestType3() {
        return labelTestType3;
    }

    public JComboBox getComboBoxTestType() {
        return comboBoxTestType;
    }

    public JLabel getLabelTestTestSource() {
        return labelTestTestSource;
    }

    public JComboBox getComboBoxTestSource() {
        return comboBoxTestSource;
    }

    public JLabel getLabelITSQRevision() {
        return labelITSQRevision;
    }

    public JComboBox getComboBoxITSQRevision() {
        return comboBoxITSQRevision;
    }

    public JCheckBox getCheckBoxDemoMode() {
        return checkBoxDemoMode;
    }

    public JButton getButtonStartProcess() {
        return buttonStartProcess;
    }

    public JButton getButtonStopUserTasksThread() {
        return buttonStopUserTasksThread;
    }

    public JLabel getLabelTestCasesPath() {
        return labelTestCasesPath;
    }

    public JTextField getTextFieldTestCasesPath() {
        return textFieldTestCasesPath;
    }

    public JCheckBox getCheckBoxUploadSynthetics() {
        return checkBoxUploadSynthetics;
    }

    public JCheckBox getCheckBoxUseOnlyTestCLZs() {
        return checkBoxUseOnlyTestCLZs;
    }

    public JLabel getLabelFachwertConfig() {
        return labelFachwertConfig;
    }

    public JRadioButton getRadioButtonFWConfigNewest() {
        return radioButtonFWConfigNewest;
    }

    public JRadioButton getRadioButtonFWConfigLikePRE() {
        return radioButtonFWConfigLikePRE;
    }

    public JLabel getLabelExportFormat() {
        return labelExportFormat;
    }

    public JRadioButton getRadioButtonExportFormatNewest() {
        return radioButtonExportFormatNewest;
    }

    public JRadioButton getRadioButtonExportFormatLikePRE() {
        return radioButtonExportFormatLikePRE;
    }

    public JSeparator getSeparator1() {
        return separator1;
    }

    public JLabel getLabelTestJobs() {
        return labelTestJobs;
    }

    public JComboBox getComboBoxTestJobs() {
        return comboBoxTestJobs;
    }

    public JLabel getLabelTestPhase() {
        return labelTestPhase;
    }

    public JComboBox getComboBoxTestPhase() {
        return comboBoxTestPhase;
    }

    public JButton getButtonStartTestJob() {
        return buttonStartTestJob;
    }

    public JLabel getLabelJobParams() {
        return labelJobParams;
    }

    public JTextField getTextFieldJobParams() {
        return textFieldJobParams;
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        ResourceBundle bundle = ResourceBundle.getBundle("de.cavdar.gui.design.form");
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

        //======== this ========
        setLayout(new GridBagLayout());
        ((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        ((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
        ((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
        ((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

        //---- labelTestType3 ----
        labelTestType3.setText(bundle.getString("TestSupportMainProcessPanel.labelTestType3.text"));
        add(labelTestType3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(4, 1, 6, 6), 0, 0));
        add(comboBoxTestType, new GridBagConstraints(1, 0, 2, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(4, 1, 6, 6), 0, 0));

        //---- labelTestTestSource ----
        labelTestTestSource.setText(bundle.getString("TestSupportMainProcessPanel.labelTestTestSource.text"));
        add(labelTestTestSource, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(4, 1, 6, 6), 0, 0));
        add(comboBoxTestSource, new GridBagConstraints(4, 0, 2, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(4, 1, 6, 6), 0, 0));

        //---- labelITSQRevision ----
        labelITSQRevision.setText(bundle.getString("TestSupportMainProcessPanel.labelITSQRevision.text"));
        add(labelITSQRevision, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(4, 1, 6, 6), 0, 0));
        add(comboBoxITSQRevision, new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(4, 1, 6, 6), 0, 0));

        //---- checkBoxDemoMode ----
        checkBoxDemoMode.setText(bundle.getString("TestSupportMainProcessPanel.checkBoxDemoMode.text"));
        add(checkBoxDemoMode, new GridBagConstraints(8, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- buttonStartProcess ----
        buttonStartProcess.setText(bundle.getString("TestSupportMainProcessPanel.buttonStartProcess.text"));
        buttonStartProcess.setIcon(new ImageIcon(getClass().getResource("/icons/gear_run.png")));
        buttonStartProcess.setMinimumSize(new Dimension(80, 24));
        buttonStartProcess.setMaximumSize(new Dimension(120, 24));
        buttonStartProcess.setPreferredSize(new Dimension(80, 24));
        add(buttonStartProcess, new GridBagConstraints(9, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(2, 1, 6, 6), 0, 0));

        //---- buttonStopUserTasksThread ----
        buttonStopUserTasksThread.setIcon(new ImageIcon(getClass().getResource("/icons/cancel.png")));
        buttonStopUserTasksThread.setActionCommand(bundle.getString("TestSupportMainProcessPanel.buttonStopUserTasksThread.actionCommand"));
        buttonStopUserTasksThread.setToolTipText(bundle.getString("TestSupportMainProcessPanel.buttonStopUserTasksThread.toolTipText"));
        buttonStopUserTasksThread.setMinimumSize(new Dimension(24, 24));
        buttonStopUserTasksThread.setMaximumSize(new Dimension(24, 24));
        buttonStopUserTasksThread.setPreferredSize(new Dimension(24, 24));
        add(buttonStopUserTasksThread, new GridBagConstraints(10, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(2, 1, 6, 1), 0, 0));

        //---- labelTestCasesPath ----
        labelTestCasesPath.setText(bundle.getString("TestSupportMainProcessPanel.labelTestCasesPath.text"));
        add(labelTestCasesPath, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(2, 1, 6, 6), 0, 0));

        //---- textFieldTestCasesPath ----
        textFieldTestCasesPath.setEditable(false);
        add(textFieldTestCasesPath, new GridBagConstraints(1, 1, 5, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(2, 1, 6, 6), 0, 0));

        //---- checkBoxUploadSynthetics ----
        checkBoxUploadSynthetics.setText(bundle.getString("TestSupportMainProcessPanel.checkBoxUploadSynthetics.text"));
        checkBoxUploadSynthetics.setSelected(true);
        add(checkBoxUploadSynthetics, new GridBagConstraints(7, 1, 2, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(2, 2, 7, 7), 0, 0));

        //---- checkBoxUseOnlyTestCLZs ----
        checkBoxUseOnlyTestCLZs.setText(bundle.getString("TestSupportMainProcessPanel.checkBoxUseOnlyTestCLZs.text"));
        checkBoxUseOnlyTestCLZs.setSelected(true);
        add(checkBoxUseOnlyTestCLZs, new GridBagConstraints(9, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(2, 2, 7, 7), 0, 0));

        //---- labelFachwertConfig ----
        labelFachwertConfig.setText(bundle.getString("TestSupportMainProcessPanel.labelFachwertConfig.text"));
        add(labelFachwertConfig, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(2, 1, 6, 6), 0, 0));

        //---- radioButtonFWConfigNewest ----
        radioButtonFWConfigNewest.setText(bundle.getString("TestSupportMainProcessPanel.radioButtonFWConfigNewest.text"));
        add(radioButtonFWConfigNewest, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(2, 1, 6, 6), 0, 0));

        //---- radioButtonFWConfigLikePRE ----
        radioButtonFWConfigLikePRE.setText(bundle.getString("TestSupportMainProcessPanel.radioButtonFWConfigLikePRE.text"));
        add(radioButtonFWConfigLikePRE, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(2, 1, 6, 6), 0, 0));

        //---- labelExportFormat ----
        labelExportFormat.setText(bundle.getString("TestSupportMainProcessPanel.labelExportFormat.text"));
        add(labelExportFormat, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(2, 1, 6, 6), 0, 0));

        //---- radioButtonExportFormatNewest ----
        radioButtonExportFormatNewest.setText(bundle.getString("TestSupportMainProcessPanel.radioButtonExportFormatNewest.text"));
        add(radioButtonExportFormatNewest, new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(2, 1, 6, 6), 0, 0));

        //---- radioButtonExportFormatLikePRE ----
        radioButtonExportFormatLikePRE.setText(bundle.getString("TestSupportMainProcessPanel.radioButtonExportFormatLikePRE.text"));
        add(radioButtonExportFormatLikePRE, new GridBagConstraints(5, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(2, 1, 6, 6), 0, 0));
        add(separator1, new GridBagConstraints(0, 3, 11, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(2, 1, 6, 1), 0, 0));

        //---- labelTestJobs ----
        labelTestJobs.setText(bundle.getString("TestSupportMainProcessPanel.labelTestJobs.text"));
        add(labelTestJobs, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(2, 1, 6, 6), 0, 0));
        add(comboBoxTestJobs, new GridBagConstraints(1, 4, 5, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(2, 1, 6, 6), 0, 0));

        //---- labelTestPhase ----
        labelTestPhase.setText(bundle.getString("TestSupportMainProcessPanel.labelTestPhase.text"));
        add(labelTestPhase, new GridBagConstraints(6, 4, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(4, 1, 6, 6), 0, 0));
        add(comboBoxTestPhase, new GridBagConstraints(7, 4, 2, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(4, 1, 6, 6), 0, 0));

        //---- buttonStartTestJob ----
        buttonStartTestJob.setText(bundle.getString("TestSupportMainProcessPanel.buttonStartTestJob.text"));
        buttonStartTestJob.setIcon(new ImageIcon(getClass().getResource("/icons/gear_run.png")));
        buttonStartTestJob.setPreferredSize(new Dimension(58, 24));
        buttonStartTestJob.setMinimumSize(new Dimension(18, 24));
        buttonStartTestJob.setMaximumSize(new Dimension(88, 24));
        add(buttonStartTestJob, new GridBagConstraints(9, 4, 2, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(2, 1, 6, 1), 0, 0));

        //---- labelJobParams ----
        labelJobParams.setText(bundle.getString("TestSupportMainProcessPanel.labelJobParams.text"));
        labelJobParams.setFocusable(false);
        add(labelJobParams, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(2, 1, 1, 6), 0, 0));
        add(textFieldJobParams, new GridBagConstraints(1, 5, 8, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(2, 1, 1, 6), 0, 0));

        //---- buttonGroup1 ----
        var buttonGroup1 = new ButtonGroup();
        buttonGroup1.add(radioButtonFWConfigNewest);
        buttonGroup1.add(radioButtonFWConfigLikePRE);

        //---- buttonGroup2 ----
        var buttonGroup2 = new ButtonGroup();
        buttonGroup2.add(radioButtonExportFormatNewest);
        buttonGroup2.add(radioButtonExportFormatLikePRE);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
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
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
