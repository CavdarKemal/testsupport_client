package de.creditreform.crefoteam.cte.tesun.gui.swingworker;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SearchForWordWorker extends SwingWorker<Integer, SearchForWordWorkerChunk> {

    private static void failIfInterrupted() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Interrupted while searching files");
        }
    }

    private final String word;
    private final File directory;
    private final String extension;
    private final SearchForWordWorkerListener listener;

    public SearchForWordWorker(final String word, final File directory, final String extension, final SearchForWordWorkerListener listener) {
        this.word = word;
        this.directory = directory;
        this.extension = extension;
        this.listener = listener;
    }

    @Override
    protected Integer doInBackground() throws Exception {
        // The number of instances the word is found
        int matches = 0;

        /*
         * List all text files under the given directory using the Apache IO library. This process cannot be
         * interrupted (stopped through cancellation). That is why we are checking right after the process whether
         * it was interrupted or not.
         */
        publish(new SearchForWordWorkerChunk("Liste alle Dateien unter dem Verzeichnis '" + directory + "'...\n"));
        final List<File> textFiles = new ArrayList<>(FileUtils.listFiles(directory, new WildcardFileFilter(extension), TrueFileFilter.TRUE));
        SearchForWordWorker.failIfInterrupted();
        publish(new SearchForWordWorkerChunk(textFiles.size() + " Dateien unter dem Verzeichnis '" + directory + "' gefunden.\n"));

        for (int i = 0, size = textFiles.size(); i < size; i++) {
            /*
             * In order to respond to the cancellations, we need to check whether this thread (the worker thread)
             * was interrupted or not. If the thread was interrupted, then we simply throw an InterruptedException
             * to indicate that the worker thread was cancelled.
             */
            SearchForWordWorker.failIfInterrupted();

            // Update the status and indicate which file is being searched.
            final File file = textFiles.get(i);
            publish(new SearchForWordWorkerChunk("Durchsuche die Datei '" + file.getAbsolutePath().replace(directory.getAbsolutePath(), "..") + "'..."));

            /*
             * Read the file content into a string, and count the matches using the Apache common IO and Lang
             * libraries respectively.
             */
            final String text = FileUtils.readFileToString(file);
            int subMatches = StringUtils.countMatches(text, word);
            publish(new SearchForWordWorkerChunk("\t--> " + subMatches + " Treffer gefunden.\n"));
            matches += subMatches;

            // Update the progress
            setProgress((i + 1) * 100 / size);
        }

        // Return the number of matches found
        return matches;
    }

    @Override
    protected void process(final List<SearchForWordWorkerChunk> chunks) {
        listener.processChunks(chunks);
    }

}