package de.creditreform.crefoteam.cte.tesun.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListSynthetischeTestdatenTest {
    @Test
    public void testListProvidedXML() throws URISyntaxException, IOException {
        List<Path> content = TestFallFileUtil.listFolderContentAsFilenames(TestFallFileUtil.FOLDERNAME_SYNTH_TESTCREFOS, ".xml");
        Assert.assertNotNull(content);
        content.stream().forEach(path -> System.out.println("/"+path.toString()));
    }
    @Test
    public void testDownloadProvidedXML() throws Exception {
        File outputDir = new File("/target/SYNTHETIC_CREFOS");
        List<File> content = TestFallFileUtil.downloadFolderContentFromFolder(TestFallFileUtil.FOLDERNAME_SYNTH_TESTCREFOS, ".xml", outputDir);
        Assert.assertNotNull(content);
        content.stream().forEach(file -> System.out.println(file.getAbsolutePath()));
    }

    public List<String> listFolderContent(String folderName) throws URISyntaxException, IOException {
        final List<String> entries = new ArrayList<>();
        // Mit nio2 können wir eine URI ermitteln, auch wenn der Ordner in einer Datei liegt
        URI uri = getClass().getClassLoader().getResource(folderName).toURI();
        if ("jar".equals(uri.getScheme())) {
            // Daten liegen innerhalb einer JAR-Datei, wir erzeugen temporär ein FileSystem auf dem JAR
            try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap(), null)) {
                // innerhalb unseres temporären Dateisystems können wir auf den Pfad des Ordners zugreifen
                Files.list(fileSystem.getPath(folderName)).forEach((p) -> entries.add(p.toString()));
                //return collectEntries(fileSystem.getPath(folderName));
            }
        } else {
            // Daten liegen _nicht_ in einer JAR-Datei...
            Files.list(Paths.get(uri)).forEach((p) -> entries.add(p.toString()));
            //return collectEntries( Paths.get(uri) );
        }
        return entries;
    }

    private List<String> collectEntries(Path folderPath) throws IOException {
        final List<String> entries = new ArrayList<>();
        Files.list(folderPath).forEach((p) -> entries.add(p.toString()));
        return entries;
    }

}