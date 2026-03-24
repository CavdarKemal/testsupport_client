/*
 * Created by JFormDesigner on Tue Feb 11 13:59:48 CET 2025
 */

package de.creditreform.crefoteam.cte.tesun.loescscanner.design;

import javax.swing.*;
import java.awt.*;

/**
 * @author CavdarK
 */
public class ScanResultDefaultPanel extends JPanel {
    public ScanResultDefaultPanel() {
        initComponents();
    }

    public JLabel getLabelDiffFilePath() {
        return labelDiffFilePath;
    }

    public JScrollPane getScrollPaneSrcFile() {
        return scrollPaneSrcFile;
    }

    public JTextArea getTextAreaFileSrc() {
        return textAreaFileSrc;
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
      labelDiffFilePath = new JLabel();
      scrollPaneSrcFile = new JScrollPane();
      textAreaFileSrc = new JTextArea();

      //======== this ========
      setLayout(new BorderLayout());

      //---- labelDiffFilePath ----
      labelDiffFilePath.setText("text");
      add(labelDiffFilePath, BorderLayout.PAGE_START);

      //======== scrollPaneSrcFile ========
      {

         //---- textAreaFileSrc ----
         textAreaFileSrc.setEditable(false);
         textAreaFileSrc.setFont(new Font("Verdana", Font.PLAIN, 12));
         scrollPaneSrcFile.setViewportView(textAreaFileSrc);
      }
      add(scrollPaneSrcFile, BorderLayout.CENTER);
      // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
   private JLabel labelDiffFilePath;
   private JScrollPane scrollPaneSrcFile;
   private JTextArea textAreaFileSrc;
   // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
