/*
 * Created by JFormDesigner on Tue Jan 28 15:54:21 CET 2025
 */

package de.creditreform.crefoteam.cte.tesun.gui.design;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;

/**
 * @author CavdarK
 */
public class ClosableTabPanel extends JPanel {
    public ClosableTabPanel() {
        initComponents();
    }

    public JButton getButtonClose() {
        return buttonClose;
    }

    public JCheckBox getCheckBoxSelect() {
        return checkBoxSelect;
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
      checkBoxSelect = new JCheckBox();
      buttonClose = new JButton();

      //======== this ========
      setBorder(new EtchedBorder());
      setLayout(new GridBagLayout());
      ((GridBagLayout)getLayout()).columnWidths = new int[] {0, 26, 0};
      ((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0};
      ((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
      ((GridBagLayout)getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

      //---- checkBoxSelect ----
      checkBoxSelect.setText("Tab-Titel");
      add(checkBoxSelect, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
         GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
         new Insets(0, 0, 0, 0), 0, 0));

      //---- buttonClose ----
      buttonClose.setIcon(new ImageIcon(getClass().getResource("/icons/cancel.png")));
      buttonClose.setMaximumSize(new Dimension(20, 20));
      buttonClose.setMinimumSize(new Dimension(20, 20));
      buttonClose.setBackground(Color.white);
      buttonClose.setBorder(null);
      buttonClose.setForeground(Color.white);
      buttonClose.setIconTextGap(1);
      buttonClose.setOpaque(false);
      buttonClose.setPreferredSize(new Dimension(10, 20));
      buttonClose.setContentAreaFilled(false);
      add(buttonClose, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
         GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
         new Insets(0, 0, 0, 0), 0, 0));
      // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
   private JCheckBox checkBoxSelect;
   private JButton buttonClose;
   // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
