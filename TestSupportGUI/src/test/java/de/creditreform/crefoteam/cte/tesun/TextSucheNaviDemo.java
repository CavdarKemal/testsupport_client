package de.creditreform.crefoteam.cte.tesun;


import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

public class TextSucheNaviDemo extends JFrame {

    private JTextArea textArea;
    private JTextField searchField;
    private JButton btnSearch, btnNext, btnPrev;
    private JLabel lblStatus;

    // Speichert die Start-Positionen aller Treffer
    private List<Integer> matchPositions = new ArrayList<>();
    private int currentMatchIndex = -1;
    private String lastSearchTerm = "";

    private Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);

    public TextSucheNaviDemo() {
        setTitle("Suche mit Navigation");
        setSize(600, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- UI Komponenten ---
        textArea = new JTextArea("Java ist eine Insel. Java ist auch eine Kaffeesorte. \n" +
                "Java Swing macht Spaß, wenn man weiß wie. Suche nach Java!");
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(15);
        btnSearch = new JButton("Alle Markieren");
        btnPrev = new JButton("< Zurück");
        btnNext = new JButton("Weiter >");
        lblStatus = new JLabel("Treffer: 0/0");

        topPanel.add(new JLabel("Suche:"));
        topPanel.add(searchField);
        topPanel.add(btnSearch);
        topPanel.add(btnPrev);
        topPanel.add(btnNext);
        topPanel.add(lblStatus);
        add(topPanel, BorderLayout.NORTH);

        // --- Event Listener ---

        // Haupt-Suche
        btnSearch.addActionListener(e -> executeSearch());

        // Enter im Suchfeld startet ebenfalls die Suche
        searchField.addActionListener(e -> executeSearch());

        // Navigation Weiter
        btnNext.addActionListener(e -> {
            if (!matchPositions.isEmpty()) {
                currentMatchIndex++;
                // Am Ende wieder vorne anfangen (Loop)
                if (currentMatchIndex >= matchPositions.size()) {
                    currentMatchIndex = 0;
                }
                navigateToCurrentMatch();
            }
        });

        // Navigation Zurück
        btnPrev.addActionListener(e -> {
            if (!matchPositions.isEmpty()) {
                currentMatchIndex--;
                // Am Anfang wieder hinten anfangen (Loop)
                if (currentMatchIndex < 0) {
                    currentMatchIndex = matchPositions.size() - 1;
                }
                navigateToCurrentMatch();
            }
        });
    }

    private void executeSearch() {
        String query = searchField.getText().toLowerCase();
        lastSearchTerm = query;
        matchPositions.clear();
        currentMatchIndex = -1;

        Highlighter highlighter = textArea.getHighlighter();
        highlighter.removeAllHighlights();

        if (query.isEmpty()) {
            lblStatus.setText("Treffer: 0/0");
            return;
        }

        String text = textArea.getText().toLowerCase();
        int index = text.indexOf(query);

        while (index >= 0) {
            matchPositions.add(index);
            try {
                highlighter.addHighlight(index, index + query.length(), painter);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            index = text.indexOf(query, index + query.length());
        }

        if (!matchPositions.isEmpty()) {
            currentMatchIndex = 0;
            navigateToCurrentMatch();
        } else {
            lblStatus.setText("Keine Treffer");
        }
    }

    private void navigateToCurrentMatch() {
        if (currentMatchIndex < 0 || matchPositions.isEmpty()) return;

        int start = matchPositions.get(currentMatchIndex);
        int length = lastSearchTerm.length();

        // Cursor auf den Treffer setzen und diesen selektieren (dadurch wird er blau)
        textArea.requestFocusInWindow();
        textArea.setCaretPosition(start + length);
        textArea.moveCaretPosition(start); // Erzeugt eine Selektion

        // Status-Text aktualisieren
        lblStatus.setText("Treffer: " + (currentMatchIndex + 1) + "/" + matchPositions.size());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TextSucheNaviDemo().setVisible(true));
    }
}