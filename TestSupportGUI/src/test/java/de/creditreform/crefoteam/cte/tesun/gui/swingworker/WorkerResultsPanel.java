/*
 * Created by JFormDesigner on Fri Jan 23 11:40:34 CET 2015
 */

package de.creditreform.crefoteam.cte.tesun.gui.swingworker;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;

public class WorkerResultsPanel extends JPanel
{
  public WorkerResultsPanel()
  {
    initComponents();
  }

  public JSplitPane getSplitPaneResults()
  {
    return splitPaneResults;
  }

  public JPanel getPanelTree()
  {
    return panelTree;
  }

  public JProgressBar getProgressBarSearch()
  {
    return progressBarSearch;
  }

  public JScrollPane getScrollPaneResultsTree()
  {
    return scrollPaneResultsTree;
  }

  public JTree getTreeResults()
  {
    return treeResults;
  }

  public JButton getButtonStartStop()
  {
    return buttonStartStop;
  }

  public JTabbedPane getTabbedPaneDetails()
  {
    return tabbedPaneDetails;
  }

  public JPanel getPanelLogs()
  {
    return panelLogs;
  }

  public JPanel getPanelControls()
  {
    return panelControls;
  }

  public JLabel getLabelLogLevel()
  {
    return labelLogLevel;
  }

  public JRadioButton getRadioButtonLogLevelERROR()
  {
    return radioButtonLogLevelERROR;
  }

  public JRadioButton getRadioButtonLogLevelWARNING()
  {
    return radioButtonLogLevelWARNING;
  }

  public JRadioButton getRadioButtonLogLevelINFO()
  {
    return radioButtonLogLevelINFO;
  }

  public JRadioButton getRadioButtonLogLevelDEBUG()
  {
    return radioButtonLogLevelDEBUG;
  }

  public JButton getButtonClear()
  {
    return buttonClear;
  }

  public JScrollPane getScrollPaneLogs()
  {
    return scrollPaneLogs;
  }

  public JTextArea getTextAreaLogs()
  {
    return textAreaLogs;
  }

  public JPanel getViewReusltsDetails()
  {
    return viewReusltsDetails;
  }

  public JPanel getPanelWorkerControls()
  {
    return panelWorkerControls;
  }

  public JLabel getLabelWorkerPath()
  {
    return labelWorkerPath;
  }

  public JTextField getTextFieldWorkerPath()
  {
    return textFieldWorkerPath;
  }

  public JButton getButtonSelectWorkerPath()
  {
    return buttonSelectWorkerPath;
  }

  public JLabel getLabelFileFilter()
  {
    return labelFileFilter;
  }

  public JCheckBox getCheckBoxRecursive()
  {
    return checkBoxRecursive;
  }

  public JTextField getTextFieldFileExtensions() {
    return textFieldFileExtensions;
  }

  private void initComponents()
  {
    // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
    panelWorkerControls = new JPanel();
    labelWorkerPath = new JLabel();
    textFieldWorkerPath = new JTextField();
    buttonSelectWorkerPath = new JButton();
    labelFileFilter = new JLabel();
    textFieldFileExtensions = new JTextField();
    checkBoxRecursive = new JCheckBox();
    buttonStartStop = new JButton();
    splitPaneResults = new JSplitPane();
    panelTree = new JPanel();
    progressBarSearch = new JProgressBar();
    scrollPaneResultsTree = new JScrollPane();
    treeResults = new JTree();
    tabbedPaneDetails = new JTabbedPane();
    panelLogs = new JPanel();
    panelControls = new JPanel();
    labelLogLevel = new JLabel();
    radioButtonLogLevelERROR = new JRadioButton();
    radioButtonLogLevelWARNING = new JRadioButton();
    radioButtonLogLevelINFO = new JRadioButton();
    radioButtonLogLevelDEBUG = new JRadioButton();
    buttonClear = new JButton();
    scrollPaneLogs = new JScrollPane();
    textAreaLogs = new JTextArea();
    viewReusltsDetails = new JPanel();

    //======== this ========
    setName("this");
    setLayout(new BorderLayout());

    //======== panelWorkerControls ========
    {
      panelWorkerControls.setName("panelWorkerControls");
      panelWorkerControls.setLayout(new GridBagLayout());
      ((GridBagLayout)panelWorkerControls.getLayout()).columnWidths = new int[] {0, 123, 105, 80, 0, 0};
      ((GridBagLayout)panelWorkerControls.getLayout()).rowHeights = new int[] {0, 0, 0};
      ((GridBagLayout)panelWorkerControls.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0, 0.0, 1.0E-4};
      ((GridBagLayout)panelWorkerControls.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

      //---- labelWorkerPath ----
      labelWorkerPath.setText("Verzeichnis:");
      labelWorkerPath.setName("labelWorkerPath");
      panelWorkerControls.add(labelWorkerPath, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(2, 2, 7, 7), 0, 0));

      //---- textFieldWorkerPath ----
      textFieldWorkerPath.setName("textFieldWorkerPath");
      panelWorkerControls.add(textFieldWorkerPath, new GridBagConstraints(1, 0, 3, 1, 1.0, 0.0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
        new Insets(2, 2, 7, 7), 0, 0));

      //---- buttonSelectWorkerPath ----
      buttonSelectWorkerPath.setIcon(new ImageIcon(getClass().getResource("/icons/folder_view.png")));
      buttonSelectWorkerPath.setName("buttonSelectWorkerPath");
      panelWorkerControls.add(buttonSelectWorkerPath, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(2, 2, 7, 2), 0, 0));

      //---- labelFileFilter ----
      labelFileFilter.setText("Extensions:");
      labelFileFilter.setName("labelFileFilter");
      panelWorkerControls.add(labelFileFilter, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(2, 2, 2, 7), 0, 0));

      //---- textFieldFileExtensions ----
      textFieldFileExtensions.setName("textFieldFileExtensions");
      panelWorkerControls.add(textFieldFileExtensions, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
        new Insets(2, 2, 2, 7), 0, 0));

      //---- checkBoxRecursive ----
      checkBoxRecursive.setText("Rekursiv");
      checkBoxRecursive.setSelected(true);
      checkBoxRecursive.setName("checkBoxRecursive");
      panelWorkerControls.add(checkBoxRecursive, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(2, 2, 2, 7), 0, 0));

      //---- buttonStartStop ----
      buttonStartStop.setIcon(new ImageIcon(getClass().getResource("/icons/replace.png")));
      buttonStartStop.setText("Suche starten");
      buttonStartStop.setName("buttonStartStop");
      panelWorkerControls.add(buttonStartStop, new GridBagConstraints(3, 1, 2, 1, 0.0, 0.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(2, 2, 2, 2), 0, 0));
    }
    add(panelWorkerControls, BorderLayout.NORTH);

    //======== splitPaneResults ========
    {
      splitPaneResults.setName("splitPaneResults");

      //======== panelTree ========
      {
        panelTree.setName("panelTree");
        panelTree.setLayout(new BorderLayout());

        //---- progressBarSearch ----
        progressBarSearch.setName("progressBarSearch");
        panelTree.add(progressBarSearch, BorderLayout.NORTH);

        //======== scrollPaneResultsTree ========
        {
          scrollPaneResultsTree.setName("scrollPaneResultsTree");

          //---- treeResults ----
          treeResults.setName("treeResults");
          scrollPaneResultsTree.setViewportView(treeResults);
        }
        panelTree.add(scrollPaneResultsTree, BorderLayout.CENTER);
      }
      splitPaneResults.setLeftComponent(panelTree);

      //======== tabbedPaneDetails ========
      {
        tabbedPaneDetails.setName("tabbedPaneDetails");

        //======== panelLogs ========
        {
          panelLogs.setName("panelLogs");
          panelLogs.setLayout(new BorderLayout());

          //======== panelControls ========
          {
            panelControls.setName("panelControls");
            panelControls.setLayout(new GridBagLayout());
            ((GridBagLayout)panelControls.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
            ((GridBagLayout)panelControls.getLayout()).rowHeights = new int[] {0, 0};
            ((GridBagLayout)panelControls.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
            ((GridBagLayout)panelControls.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

            //---- labelLogLevel ----
            labelLogLevel.setText("LOG-Level:");
            labelLogLevel.setName("labelLogLevel");
            panelControls.add(labelLogLevel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
              GridBagConstraints.CENTER, GridBagConstraints.BOTH,
              new Insets(2, 2, 2, 7), 0, 0));

            //---- radioButtonLogLevelERROR ----
            radioButtonLogLevelERROR.setText("ERROR");
            radioButtonLogLevelERROR.setName("radioButtonLogLevelERROR");
            panelControls.add(radioButtonLogLevelERROR, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
              GridBagConstraints.CENTER, GridBagConstraints.BOTH,
              new Insets(2, 2, 2, 7), 0, 0));

            //---- radioButtonLogLevelWARNING ----
            radioButtonLogLevelWARNING.setText("WARNING");
            radioButtonLogLevelWARNING.setName("radioButtonLogLevelWARNING");
            panelControls.add(radioButtonLogLevelWARNING, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
              GridBagConstraints.CENTER, GridBagConstraints.BOTH,
              new Insets(2, 2, 2, 7), 0, 0));

            //---- radioButtonLogLevelINFO ----
            radioButtonLogLevelINFO.setText("INFO");
            radioButtonLogLevelINFO.setSelected(true);
            radioButtonLogLevelINFO.setName("radioButtonLogLevelINFO");
            panelControls.add(radioButtonLogLevelINFO, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
              GridBagConstraints.CENTER, GridBagConstraints.BOTH,
              new Insets(2, 2, 2, 7), 0, 0));

            //---- radioButtonLogLevelDEBUG ----
            radioButtonLogLevelDEBUG.setText("DEBUG");
            radioButtonLogLevelDEBUG.setName("radioButtonLogLevelDEBUG");
            panelControls.add(radioButtonLogLevelDEBUG, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
              GridBagConstraints.CENTER, GridBagConstraints.BOTH,
              new Insets(2, 2, 2, 7), 0, 0));

            //---- buttonClear ----
            buttonClear.setText("L\u00f6schen");
            buttonClear.setName("buttonClear");
            panelControls.add(buttonClear, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0,
              GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
              new Insets(2, 2, 2, 2), 0, 0));
          }
          panelLogs.add(panelControls, BorderLayout.NORTH);

          //======== scrollPaneLogs ========
          {
            scrollPaneLogs.setName("scrollPaneLogs");

            //---- textAreaLogs ----
            textAreaLogs.setEditable(false);
            textAreaLogs.setName("textAreaLogs");
            scrollPaneLogs.setViewportView(textAreaLogs);
          }
          panelLogs.add(scrollPaneLogs, BorderLayout.CENTER);
        }
        tabbedPaneDetails.addTab("Logs", panelLogs);


        //======== viewReusltsDetails ========
        {
          viewReusltsDetails.setName("viewReusltsDetails");
          viewReusltsDetails.setLayout(new BorderLayout());
        }
        tabbedPaneDetails.addTab("Details", viewReusltsDetails);

      }
      splitPaneResults.setRightComponent(tabbedPaneDetails);
    }
    add(splitPaneResults, BorderLayout.CENTER);

    //---- buttonGroup ----
    ButtonGroup buttonGroup = new ButtonGroup();
    buttonGroup.add(radioButtonLogLevelERROR);
    buttonGroup.add(radioButtonLogLevelWARNING);
    buttonGroup.add(radioButtonLogLevelINFO);
    buttonGroup.add(radioButtonLogLevelDEBUG);
    // JFormDesigner - End of component initialization  //GEN-END:initComponents
  }

  // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
  private JPanel panelWorkerControls;
  private JLabel labelWorkerPath;
  private JTextField textFieldWorkerPath;
  private JButton buttonSelectWorkerPath;
  private JLabel labelFileFilter;
  private JTextField textFieldFileExtensions;
  private JCheckBox checkBoxRecursive;
  private JButton buttonStartStop;
  private JSplitPane splitPaneResults;
  private JPanel panelTree;
  private JProgressBar progressBarSearch;
  private JScrollPane scrollPaneResultsTree;
  private JTree treeResults;
  private JTabbedPane tabbedPaneDetails;
  private JPanel panelLogs;
  private JPanel panelControls;
  private JLabel labelLogLevel;
  private JRadioButton radioButtonLogLevelERROR;
  private JRadioButton radioButtonLogLevelWARNING;
  private JRadioButton radioButtonLogLevelINFO;
  private JRadioButton radioButtonLogLevelDEBUG;
  private JButton buttonClear;
  private JScrollPane scrollPaneLogs;
  private JTextArea textAreaLogs;
  private JPanel viewReusltsDetails;
  // JFormDesigner - End of variables declaration  //GEN-END:variables
}
