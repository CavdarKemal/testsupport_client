/*
 * Created by JFormDesigner on Thu Oct 10 14:15:41 CEST 2019
 */

package de.creditreform.crefoteam.cte.tesun.gui.swingworker;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * @author Kemal Cavdar
 */
public class SearchForWordWorkerFrame extends JFrame {
   public SearchForWordWorkerFrame() {
      initComponents();
   }

   public JPanel getPanelControls() {
      return panelControls;
   }

   public JLabel getLabelFile() {
      return labelFile;
   }

   public JTextField getTextFieldFile() {
      return textFieldFile;
   }

   public JButton getButtonFile() {
      return buttonFile;
   }

   public JLabel getLabelSearchFor() {
      return labelSearchFor;
   }

   public JTextField getTextFieldSearchFor() {
      return textFieldSearchFor;
   }

   public JButton getButtonStartStopSearch() {
      return buttonStartStopSearch;
   }

   public JPanel getPanelSearchResult() {
      return panelSearchResult;
   }

   public JScrollPane getScrollPaneSearchResults() {
      return scrollPaneSearchResults;
   }

   public JTextArea getTextAreaSearchResults() {
      return textAreaSearchResults;
   }

   public JProgressBar getProgressBarSearching() {
      return progressBarSearching;
   }

   public JLabel getLabelExtensions() {
      return labelExtensions;
   }

   public JTextField getTextFieldExtensions() {
      return textFieldExtensions;
   }

   private void initComponents() {
      // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
      panelControls = new JPanel();
      labelFile = new JLabel();
      textFieldFile = new JTextField();
      buttonFile = new JButton();
      labelExtensions = new JLabel();
      textFieldExtensions = new JTextField();
      labelSearchFor = new JLabel();
      textFieldSearchFor = new JTextField();
      buttonStartStopSearch = new JButton();
      progressBarSearching = new JProgressBar();
      panelSearchResult = new JPanel();
      scrollPaneSearchResults = new JScrollPane();
      textAreaSearchResults = new JTextArea();

      //======== this ========
      setName("this");
      Container contentPane = getContentPane();
      contentPane.setLayout(new BorderLayout());

      //======== panelControls ========
      {
         panelControls.setName("panelControls");
         panelControls.setLayout(new GridBagLayout());
         ((GridBagLayout)panelControls.getLayout()).columnWidths = new int[] {0, 77, 52, 292, 30, 0};
         ((GridBagLayout)panelControls.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
         ((GridBagLayout)panelControls.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0, 0.0, 1.0E-4};
         ((GridBagLayout)panelControls.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};

         //---- labelFile ----
         labelFile.setText("Verzeichnis:");
         labelFile.setName("labelFile");
         panelControls.add(labelFile, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(2, 1, 6, 6), 0, 0));

         //---- textFieldFile ----
         textFieldFile.setName("textFieldFile");
         panelControls.add(textFieldFile, new GridBagConstraints(1, 0, 3, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(2, 1, 6, 6), 0, 0));

         //---- buttonFile ----
         buttonFile.setIcon(new ImageIcon(getClass().getResource("/icons/folder_up.png")));
         buttonFile.setPreferredSize(new Dimension(34, 24));
         buttonFile.setName("buttonFile");
         panelControls.add(buttonFile, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(2, 1, 6, 1), 0, 0));

         //---- labelExtensions ----
         labelExtensions.setText("Extensions:");
         labelExtensions.setName("labelExtensions");
         panelControls.add(labelExtensions, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(1, 1, 6, 6), 0, 0));

         //---- textFieldExtensions ----
         textFieldExtensions.setName("textFieldExtensions");
         panelControls.add(textFieldExtensions, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(1, 1, 6, 6), 0, 0));

         //---- labelSearchFor ----
         labelSearchFor.setText("Suchbegriff:");
         labelSearchFor.setName("labelSearchFor");
         panelControls.add(labelSearchFor, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(1, 1, 6, 6), 0, 0));

         //---- textFieldSearchFor ----
         textFieldSearchFor.setName("textFieldSearchFor");
         panelControls.add(textFieldSearchFor, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(1, 1, 6, 6), 0, 0));

         //---- buttonStartStopSearch ----
         buttonStartStopSearch.setText("Suche starten");
         buttonStartStopSearch.setIcon(new ImageIcon(getClass().getResource("/icons/folder_view.png")));
         buttonStartStopSearch.setName("buttonStartStopSearch");
         panelControls.add(buttonStartStopSearch, new GridBagConstraints(1, 2, 3, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(1, 1, 6, 6), 0, 0));

         //---- progressBarSearching ----
         progressBarSearching.setBorder(new BevelBorder(BevelBorder.LOWERED));
         progressBarSearching.setName("progressBarSearching");
         panelControls.add(progressBarSearching, new GridBagConstraints(0, 3, 5, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 2, 0, 2), 0, 0));
      }
      contentPane.add(panelControls, BorderLayout.NORTH);

      //======== panelSearchResult ========
      {
         panelSearchResult.setBorder(new EtchedBorder());
         panelSearchResult.setName("panelSearchResult");
         panelSearchResult.setLayout(new BorderLayout());

         //======== scrollPaneSearchResults ========
         {
            scrollPaneSearchResults.setName("scrollPaneSearchResults");

            //---- textAreaSearchResults ----
            textAreaSearchResults.setName("textAreaSearchResults");
            scrollPaneSearchResults.setViewportView(textAreaSearchResults);
         }
         panelSearchResult.add(scrollPaneSearchResults, BorderLayout.CENTER);
      }
      contentPane.add(panelSearchResult, BorderLayout.CENTER);
      pack();
      setLocationRelativeTo(getOwner());
      // JFormDesigner - End of component initialization  //GEN-END:initComponents
   }

   // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
   private JPanel panelControls;
   private JLabel labelFile;
   private JTextField textFieldFile;
   private JButton buttonFile;
   private JLabel labelExtensions;
   private JTextField textFieldExtensions;
   private JLabel labelSearchFor;
   private JTextField textFieldSearchFor;
   private JButton buttonStartStopSearch;
   private JProgressBar progressBarSearching;
   private JPanel panelSearchResult;
   private JScrollPane scrollPaneSearchResults;
   private JTextArea textAreaSearchResults;
   // JFormDesigner - End of variables declaration  //GEN-END:variables
}
