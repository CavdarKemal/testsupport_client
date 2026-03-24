package de.creditreform.crefoteam.cte.tesun.gui.swingworker;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;
import java.util.concurrent.CancellationException;
import javax.swing.*;

public class SearchForWordWorkerView extends SearchForWordWorkerFrame implements SearchForWordWorkerListener {
   private String lastSelectedPath;
   private SearchForWordWorker searchWorker;

   public SearchForWordWorkerView() {
      initModel();
      initControls();
      initListeners();
   }

   private void initControls() {
      getTextFieldFile().setText(lastSelectedPath);
      getTextFieldExtensions().setText("*.xml");
      getTextFieldSearchFor().setText("crefo");
   }

   private void initModel() {
      lastSelectedPath = System.getProperty("user.dir");
   }

   private void initListeners() {
      getButtonFile().addActionListener(actionListener -> {
         doSelectFile();
      });
      getButtonStartStopSearch().addActionListener(actionListener -> {
         doStartSearch();
      });
   }

   private void doSelectFile() {
      String choosenFileName = chooseDirectory(this, lastSelectedPath, "");
      if(choosenFileName != null) {
         lastSelectedPath = choosenFileName;
         getTextFieldFile().setText(lastSelectedPath);
      }
   }

   private void doStartSearch() {
      if(searchWorker != null) {
         searchWorker.cancel(true);
         enableContorls(true);
         searchWorker = null;
         return;
      }
      enableContorls(false);
      final String word = getTextFieldSearchFor().getText();
      final File directory = new File(getTextFieldFile().getText());
      searchWorker = new SearchForWordWorker(word, directory, getTextFieldExtensions().getText(), this);
      searchWorker.addPropertyChangeListener(new PropertyChangeListener() {
         @Override
         public void propertyChange(final PropertyChangeEvent event) {
            switch (event.getPropertyName()) {
               case "progress":
                  getProgressBarSearching().setIndeterminate(false);
                  getProgressBarSearching().setValue((Integer) event.getNewValue());
                  break;
               case "state":
                  switch ((SwingWorker.StateValue) event.getNewValue()) {
                     case DONE:
                        try {
                           final int count = searchWorker.get();
                           JOptionPane.showMessageDialog(SearchForWordWorkerView.this, count + " Wörter gefunden.", "Suche Wörter", JOptionPane.INFORMATION_MESSAGE);
                        } catch (final CancellationException e) {
                           // JOptionPane.showMessageDialog(SearchForWordWorkerView.this, "Die Suche wurde vom Benutzer abgebrochen", "Suche Wörter", JOptionPane.WARNING_MESSAGE);
                        } catch (final Exception e) {
                           JOptionPane.showMessageDialog(SearchForWordWorkerView.this, "Die Suche wurde mit einem Fehler abgebrochen!", "Suche Wörter", JOptionPane.ERROR_MESSAGE);
                        }
                        enableContorls(true);
                        searchWorker = null;
                        break;
                     case STARTED:
                     case PENDING:
                        break;
                  }
                  break;
            }
         }
      });
      searchWorker.execute();
   }

   private void enableContorls(boolean enabled) {
      getTextFieldFile().setEnabled(enabled);
      getTextFieldExtensions().setEnabled(enabled);
      getTextFieldSearchFor().setEnabled(enabled);
      if(!enabled) {
         getTextAreaSearchResults().setText("");
      }
      getTextAreaSearchResults().setEnabled(enabled);
      getButtonFile().setEnabled(enabled);
      setCursor(Cursor.getPredefinedCursor(enabled ? Cursor.DEFAULT_CURSOR : Cursor.WAIT_CURSOR));
      getButtonStartStopSearch().setText(enabled ? "Suche starten" : "Suche abbrechen");
      getProgressBarSearching().setValue(0);
   }

   public static String chooseDirectory(Component parent, String fullPath, String dialogTitle) {
      String choosenDir = "";
      JFileChooser chooser = new JFileChooser(fullPath);
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      chooser.setDialogTitle(dialogTitle);
      int option = chooser.showOpenDialog(parent);
      if (option == JFileChooser.APPROVE_OPTION) {
         File file = chooser.getSelectedFile();
         if (file != null) {
            choosenDir = chooser.getSelectedFile().getAbsolutePath();
         }
      }
      return choosenDir;
   }

   public static void main(String[] args) {
      SearchForWordWorkerView searchForWordWorkerView = new SearchForWordWorkerView();
      searchForWordWorkerView.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      searchForWordWorkerView.pack();
      searchForWordWorkerView.setSize(new Dimension(1200, 800));
      searchForWordWorkerView.setLocationRelativeTo(null);
      searchForWordWorkerView.setVisible(true);
   }

   @Override
   public void processChunks(List<SearchForWordWorkerChunk> chunks) {
      chunks.stream().forEach(s -> getTextAreaSearchResults().append(s.getChunkInfo()));
   }
}
