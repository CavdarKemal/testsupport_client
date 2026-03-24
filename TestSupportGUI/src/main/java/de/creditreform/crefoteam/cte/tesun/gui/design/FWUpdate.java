/*
 * Created by JFormDesigner on Mon Jul 27 13:30:51 CEST 2020
 */

package de.creditreform.crefoteam.cte.tesun.gui.design;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import java.awt.*;

/**
 * @author Kemal Cavdar
 */
public class FWUpdate extends JPanel {
    public FWUpdate() {
        initComponents();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        label1 = new JLabel();
        label2 = new JLabel();
        comboBox1 = new JComboBox();
        label3 = new JLabel();
        textField3 = new JTextField();
        button1 = new JButton();
        label5 = new JLabel();
        textField1 = new JTextField();
        label6 = new JLabel();
        textField2 = new JTextField();

        //======== this ========
        setBorder(new CompoundBorder(
           new BevelBorder(BevelBorder.LOWERED),
           null));
        setLayout(new GridBagLayout());
        ((GridBagLayout)getLayout()).columnWidths = new int[] {98, 267, 0, 0};
        ((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0};
        ((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
        ((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

        //---- label1 ----
        label1.setText("Fachwertverwaltung - Import");
        label1.setFont(new Font("Arial Rounded MT Bold", label1.getFont().getStyle(), label1.getFont().getSize() + 2));
        add(label1, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(2, 2, 7, 2), 0, 0));

        //---- label2 ----
        label2.setText("Fachwert:");
        add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(2, 2, 7, 7), 0, 0));
        add(comboBox1, new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(2, 2, 7, 2), 0, 0));

        //---- label3 ----
        label3.setText("Datei:");
        add(label3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(2, 2, 7, 7), 0, 0));
        add(textField3, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(0, 0, 5, 5), 0, 0));

        //---- button1 ----
        button1.setText("Upload");
        button1.setPreferredSize(new Dimension(78, 26));
        add(button1, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(2, 2, 7, 2), 0, 0));

        //---- label5 ----
        label5.setText("Datum:");
        add(label5, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(2, 2, 7, 7), 0, 0));
        add(textField1, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(2, 2, 7, 7), 0, 0));

        //---- label6 ----
        label6.setText("Bezeichnung:");
        add(label6, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(2, 2, 2, 7), 0, 0));
        add(textField2, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(2, 2, 2, 7), 0, 0));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel label1;
    private JLabel label2;
    private JComboBox comboBox1;
    private JLabel label3;
    private JTextField textField3;
    private JButton button1;
    private JLabel label5;
    private JTextField textField1;
    private JLabel label6;
    private JTextField textField2;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
