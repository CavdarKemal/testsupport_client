/*
 * Created by JFormDesigner on Mon Jul 27 13:37:34 CEST 2020
 */

package de.creditreform.crefoteam.cte.tesun.gui.design;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * @author Kemal Cavdar
 */
public class FWKonfigs extends JPanel {
    public FWKonfigs() {
        initComponents();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        label1 = new JLabel();
        label2 = new JLabel();
        scrollPane1 = new JScrollPane();
        table1 = new JTable();
        label3 = new JLabel();
        textField1 = new JTextField();
        label4 = new JLabel();
        textField2 = new JTextField();
        label5 = new JLabel();
        textField3 = new JTextField();
        label6 = new JLabel();
        textField5 = new JTextField();
        label7 = new JLabel();
        textField4 = new JTextField();
        button1 = new JButton();
        button3 = new JButton();
        button2 = new JButton();
        button4 = new JButton();

        //======== this ========
        setLayout(new GridBagLayout());
        ((GridBagLayout) getLayout()).columnWidths = new int[]{0, 0, 0, 0, 266, 42, 100, 86, 0};
        ((GridBagLayout) getLayout()).rowHeights = new int[]{0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        ((GridBagLayout) getLayout()).columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 1.0E-4};
        ((GridBagLayout) getLayout()).rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 1.0E-4};

        //---- label1 ----
        label1.setText("Benannte konfigurationen");
        label1.setFont(new Font("Segoe UI Historic", Font.BOLD | Font.ITALIC, 20));
        add(label1, new GridBagConstraints(0, 0, 5, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(2, 2, 7, 7), 0, 0));

        //---- label2 ----
        label2.setText("Konfiguration");
        label2.setFont(new Font("Segoe UI Historic", Font.BOLD | Font.ITALIC, 20));
        add(label2, new GridBagConstraints(5, 0, 3, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(2, 2, 7, 2), 0, 0));

        //======== scrollPane1 ========
        {

            //---- table1 ----
            table1.setModel(new DefaultTableModel(
                    new Object[][]{
                            {"Konfig-3", "03.09.2016", null, null, true},
                            {"Konfig-2", "11.04.208", null, null, true},
                            {"Konfig-1", "02.02.2019", null, null, true},
                            {"Konfig-4", "11.01.2019", null, null, true},
                            {"-------", null, null, null, false},
                    },
                    new String[]{
                            "Bezeichnung", "Datum", "CT-Version", "WKey-Version", "Status"
                    }
            ) {
                final Class<?>[] columnTypes = new Class<?>[]{
                        String.class, String.class, Object.class, Object.class, Boolean.class
                };

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnTypes[columnIndex];
                }
            });
            table1.setAutoCreateRowSorter(true);
            scrollPane1.setViewportView(table1);
        }
        add(scrollPane1, new GridBagConstraints(0, 2, 5, 7, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(2, 2, 7, 7), 0, 0));

        //---- label3 ----
        label3.setText("Bezeichnung:");
        add(label3, new GridBagConstraints(5, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(2, 2, 7, 7), 0, 0));
        add(textField1, new GridBagConstraints(6, 2, 2, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(2, 2, 7, 2), 0, 0));

        //---- label4 ----
        label4.setText("CT-Version:");
        add(label4, new GridBagConstraints(5, 3, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(2, 2, 7, 7), 0, 0));
        add(textField2, new GridBagConstraints(6, 3, 2, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(2, 2, 7, 2), 0, 0));

        //---- label5 ----
        label5.setText("WKey-Version:");
        add(label5, new GridBagConstraints(5, 4, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(2, 2, 7, 7), 0, 0));
        add(textField3, new GridBagConstraints(6, 4, 2, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(2, 2, 7, 2), 0, 0));

        //---- label6 ----
        label6.setText("Datum:");
        add(label6, new GridBagConstraints(5, 5, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(2, 2, 7, 7), 0, 0));
        add(textField5, new GridBagConstraints(6, 5, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 5), 0, 0));

        //---- label7 ----
        label7.setText("Datei:");
        add(label7, new GridBagConstraints(5, 6, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 5), 0, 0));
        add(textField4, new GridBagConstraints(6, 6, 2, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(2, 2, 7, 2), 0, 0));

        //---- button1 ----
        button1.setText("Upload");
        button1.setPreferredSize(new Dimension(78, 26));
        add(button1, new GridBagConstraints(7, 7, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(2, 2, 7, 2), 0, 0));

        //---- button3 ----
        button3.setText("Neu");
        button3.setPreferredSize(new Dimension(78, 26));
        add(button3, new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(2, 2, 2, 7), 0, 0));

        //---- button2 ----
        button2.setText("Edit");
        button2.setPreferredSize(new Dimension(78, 26));
        add(button2, new GridBagConstraints(1, 9, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(2, 2, 2, 7), 0, 0));

        //---- button4 ----
        button4.setText("L\u00f6schen");
        button4.setPreferredSize(new Dimension(78, 26));
        add(button4, new GridBagConstraints(2, 9, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(2, 2, 2, 7), 0, 0));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel label1;
    private JLabel label2;
    private JScrollPane scrollPane1;
    private JTable table1;
    private JLabel label3;
    private JTextField textField1;
    private JLabel label4;
    private JTextField textField2;
    private JLabel label5;
    private JTextField textField3;
    private JLabel label6;
    private JTextField textField5;
    private JLabel label7;
    private JTextField textField4;
    private JButton button1;
    private JButton button3;
    private JButton button2;
    private JButton button4;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
