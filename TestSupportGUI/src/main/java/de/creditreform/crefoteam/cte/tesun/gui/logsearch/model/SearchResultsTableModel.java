package de.creditreform.crefoteam.cte.tesun.gui.logsearch.model;

import de.creditreform.crefoteam.cte.tesun.gui.base.model.CteAbstractTableModel;
import de.creditreform.crefoteam.cte.tesun.logsearch.LogEntry;

import java.sql.Date;
import java.util.List;

public class SearchResultsTableModel extends CteAbstractTableModel {
    public static final String[] COLUMNS_FOR_TABLE = new String[]{
            "Datum", "Typ", "Package", "Infos", "Log-File"
    };

    public SearchResultsTableModel(List<LogEntry> logEntriesList) {
        super(logEntriesList);
        COL_NAMES = COLUMNS_FOR_TABLE;
        fireTableDataChanged(); // Tabelle nach dem Laden der Daten aktualisieren
    }

    @Override
    public boolean isRowActivated(int rowIndex) {
        return false;
    }

    @Override
    public void setRowActivated(int rowIndex, boolean isActivated) {
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false; // columnIndex == 0;
    }

    public Class getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return Date.class;
        } else {
            return super.getColumnClass(columnIndex);
        }
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= getRowCount()) {
            return "";
        }
        LogEntry logEntry = (LogEntry) getRow(rowIndex);
        // { "Datum", "Typ", "Package", "Infos", };
        switch (columnIndex) {
            case 0: {
                return logEntry.getLogDate();
            }
            case 1: {
                return logEntry.getType();
            }
            case 2: {
                return logEntry.getPackg();
            }
            case 3: {
                return logEntry.getInfoList();
            }
            case 4: {
                return logEntry.getLogFile().getName();
            }
        }
        return "";
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= getRowCount()) {
            return;
        }
        LogEntry logEntry = (LogEntry) getRow(rowIndex);
        // { "Datum", "Typ", "Package", "Infos", };
        try {
            switch (columnIndex) {
                case 0: {
                }
                break;
                case 1: {
                }
                break;
            }
        } catch (Exception ex) {
            new RuntimeException(ex);
        }
    }

}
