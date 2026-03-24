package de.creditreform.crefoteam.cte.tesun.util.propertyfiles;

import de.creditreform.crefoteam.cte.tesun.util.OrderedProperties;
import de.creditreform.crefoteam.cte.tesun.util.directorytree.AcceptPropertyFilesFileFilter;
import de.creditreform.crefoteam.cte.tesun.util.directorytree.FileActionFactoryCollectEntries;
import de.creditreform.crefoteam.cte.tesun.util.directorytree.TreeProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Utility-Klasse zum Laden eines Property-Objektes aus einer Datei
 * User: ralf
 * Date: 25.02.14
 * Time: 15:44
 */
public class PropertyFileLoader {
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private final Charset charset;

    public PropertyFileLoader() {
        this(DEFAULT_CHARSET);
    }

    public PropertyFileLoader(Charset charset) {
        this.charset = charset;
    }

    public OrderedProperties getProperties(String filePath) throws IOException {
        return getProperties(new File(filePath));
    }

    public OrderedProperties getProperties(File f) throws IOException {
        FileInputStream in = null;
        try {
            in = new FileInputStream(f);
            OrderedProperties properties = getProperties(in);
            return properties;
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public OrderedProperties getProperties(InputStream in)
            throws IOException {
        OrderedProperties orderedProperties = new OrderedProperties(charset);
        orderedProperties.load(in);
        return orderedProperties;
    }

    /**
     * Iteriere über alle Property-Dateien im angegebenen Verzeichnis und
     * die darin enthaltenen Crefo-Nummern. Alle Treffer werden an die
     * übergebene Function weitergereicht. Ein Logger wird automatisch für
     * die Klasse der übergebenen Funktion erzeugt.
     *
     * @param baseDir zu durchsuchendes Verzeichnis
     * @param func    Function zur Verarbeitung der Treffer
     * @param <T>     Typ der Function, üblicherweise PropertyFileLoaderFunction
     */
    public <T extends PropertyFileLoaderFunction> void iterateCrefos(String baseDir, T func) throws Exception {
        final Logger logger = LoggerFactory.getLogger(func.getClass());
        iterateCrefos(logger, baseDir, func);
    }

    /**
     * Iteriere über alle Property-Dateien im angegebenen Verzeichnis und
     * die darin enthaltenen Crefo-Nummern. Alle Treffer werden an die
     * übergebene Function weitergereicht.
     *
     * @param logger  vorgabe für den zu verwendenden Logger
     * @param baseDir zu durchsuchendes Verzeichnis
     * @param func    Function zur Verarbeitung der Treffer
     * @param <T>     Typ der Function, üblicherweise PropertyFileLoaderFunction
     */
    public <T extends PropertyFileLoaderFunction> void iterateCrefos(final Logger logger, String baseDir, T func) throws Exception {
        iterateCrefos(logger, baseDir, new PropertyFileLoaderAdapterSingle(func));
    }

    public <T extends PropertyFileLoaderBulkFunction> void iterateCrefos(final Logger logger, String baseDir, T func) throws Exception {
        iterateCrefos(logger, baseDir, new PropertyFileLoaderAdapterBulk(func));
    }

    protected <T extends PropertyFileLoaderAdapter> void iterateCrefos(final Logger logger, String baseDir, T func) throws Exception {
        func.init(logger);
        File srcDir = new File(baseDir);
        if (!srcDir.exists()) {
            logger.error("Verzeichnis {} existiert nicht", baseDir);
        } else {
            FileActionFactoryCollectEntries collectedEntries = collectFiles(new AcceptPropertyFilesFileFilter(), srcDir);
            func.reset();
            for (File file : collectedEntries.getFiles()) {
                final OrderedProperties orderedProperties = getProperties(file);
                final PathInfo pathInfo = new PathInfo(srcDir, file);
                for (Map.Entry<Object, Object> e : orderedProperties.entrySet()) {
                    Long crf = toCrf(logger, e.getValue());
                    if (crf != null) {
                        String valueOf = String.valueOf(e.getKey());
                        func.collectOrProcess(pathInfo, valueOf, crf);
                    }
                }
            }
            func.processCollected();
        }
        func.shutdown();
    }

    /**
     * Sammle alle Dateien im angegebenen Verzeichnis sowie in den
     * Unterverzeichnissen
     *
     * @param fileFilter Filter für die Dateien
     * @param srcDir     Basis-Verzeichnis
     * @return FileActionFactoryCollectEntries
     * @throws Exception
     */
    public FileActionFactoryCollectEntries collectFiles(FileFilter fileFilter, File srcDir) throws Exception {
        FileActionFactoryCollectEntries collectingFactory = new FileActionFactoryCollectEntries();
        TreeProcessor tp = new TreeProcessor(fileFilter, srcDir, collectingFactory);
        tp.call();
        return collectingFactory;
    }

    /**
     * Versuche ein Object in eine Crefonummer umzuwandeln. Das Ergebnis
     * ist 'null', wenn dies nicht gelingt
     *
     * @param logger Logger für Warnmeldungen
     * @param o      umzuwandelndes Objekt
     * @return Long-Wert der Crefo oder null
     */
    public Long toCrf(Logger logger, Object o) {
        Long crf;
        if (!(o instanceof String)) {
            crf = null;
        } else {
            try {
                final String[] split = ((String) o).trim().split("#");
                final Long longValue = Long.valueOf(split[0].trim());
                if (longValue < 1000000000L || longValue > 9999999999L) {
                    logger.warn("Zahlenwert ausserhalb des erlaubten Bereiches wird nicht verarbeitet: " + longValue);
                    crf = null;
                } else {
                    crf = longValue;
                }
            } catch (NumberFormatException e) {
                crf = null;
            }
        }
        return crf;
    }

}
