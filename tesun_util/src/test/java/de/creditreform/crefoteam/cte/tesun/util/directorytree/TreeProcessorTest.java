package de.creditreform.crefoteam.cte.tesun.util.directorytree;

import org.junit.Test;

import java.io.File;
import java.io.FileFilter;

public class TreeProcessorTest {

    @Test
    public void testTreeProcessor() throws Exception {
        FileActionFactoryCollectEntries collectingFactory = new FileActionFactoryCollectEntries();
        File srcDir = new File(".");
        TreeProcessor tp = new TreeProcessor(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return true;
            }
        }, srcDir, collectingFactory);
        tp.call();
    }
}
