package de.creditreform.crefoteam.cte.tesun.util.replacer;


import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility-Klasse für die Ersetzung eines oder mehrerer Texte
 * User: ralf
 * Date: 07.02.14
 * Time: 13:55
 */
public class Replacer {
    // Byte und Byte[] ist veränderbar, ENCODING_PREFIX und END_ENCODING müssen 'private' bleiben!
    private static final byte[] ENCODING_PREFIX = "encoding=\"".getBytes(StandardCharsets.US_ASCII);
    private static final byte END_ENCODING = "\"".getBytes(StandardCharsets.US_ASCII)[0];

    private static final Logger logger = LoggerFactory.getLogger(Replacer.class);
    public static final Charset CHARSET_UTF8 = StandardCharsets.UTF_8;

    private final Map<Pattern, BiFunction<String, String, String>> patternMap;
    private final Map<String, ReplacementMapping> replacementMappingMap;

    public Replacer(Map<Pattern, BiFunction<String, String, String>> patternMap, Map<String, ReplacementMapping> replacementMappingMap, String charsetName) {
        this.patternMap = patternMap;
        // Hallo Sonar: Die als Parameter übergebene Map wird in einer BiFunction in der PatternMap gefüllt, sie muss auf
        // jeden Fall 1:1 übernommen werden. Eine Kopie funktioniert nicht!
        this.replacementMappingMap = replacementMappingMap;
    }

    public Map<Pattern, BiFunction<String, String, String>> getPatternMap() {
        return patternMap;
    }

    public Map<String, ReplacementMapping> getReplacementMappingMap() {
        return replacementMappingMap;
    }

    /**
     * Erzetzung unter Verwendung zweier Dateien, für beide Dateien kommt das
     * bei der Konstruktion übergebene Encoding zum Einsatz
     *
     * @param inFile  Pfad der Quell-Datei
     * @param outFile Pfad der Ziel-Datei
     * @throws IOException bei Fehlerm im Dateizugriff
     */
    public void copyAndReplace(String keyOriginalMap, String inFile, String outFile) throws IOException {
        final FileOutputStream outStream = new FileOutputStream(outFile);
        final FileInputStream inStream = new FileInputStream(inFile);
        try {
            copyAndReplace(keyOriginalMap, inStream, outStream);
        } finally {
            try {
                outStream.flush();
                outStream.close();
            } catch (IOException e) {
                // intentionally ignored
            }
            try {
                inStream.close();
            } catch (IOException e) {
                // intentionally ignored
            }
        }
    }

    /**
     * Erzetzung unter Verwendung zweier Streams, für beide Streams kommt das
     * bei der Konstruktion übergebene Encoding zum Einsatz
     *
     * @param inStream  InputStream mit den Quell-Daten
     * @param outStream OutputStream für die Ziel-Daten
     * @throws IOException bei Fehlern im Stream-Zugriff
     */
    public void copyAndReplace(String keyOriginalMap, InputStream inStream, OutputStream outStream) throws IOException {
        final BufferedInputStream bufferedInput = new BufferedInputStream(inStream);
        final String currentCharset = detectCharset(bufferedInput);
        String inString = IOUtils.toString(bufferedInput, currentCharset);
        StringBuilder sb = replace(keyOriginalMap, inString);
        // ACHTUNG: Unabhängig vom erkannten Zeichensatz der Eingangs-Daten speichern wir das Ergebnis mit
        //          dem vorab ausgewählten Charset (UTF-8). Hintergrund ist, dass die nachfolgende Verarbeitung
        //          ebenfalls mit UTF-8 arbeitet. Eine Unterscheidung verschiedener Zeichensätze ist für die
        //          nachfolgenden Verarbeitungs-Schritte derzeit nicht eingeplant. Die weitere Entwicklung muss
        //          erweisen, ob diese Vorgehensweise tragbar bleibt. Andernfalls müsste eine einheitliche
        //          Kennzeichnung (zum Beispiel über den Namen der ausgegebenen Datei) oder eine Überarbeitung der
        //          Inhalte (Unmarshalling/Marshalling via DOM) erfolgen.
        Writer outStreamWriter = new OutputStreamWriter(outStream, CHARSET_UTF8);
        Writer wr = new BufferedWriter(outStreamWriter);
        wr.write(sb.toString());
        wr.flush();
    }

    protected String detectCharset(BufferedInputStream bufferedInput) throws IOException {
        // Lese die ersten 500 Bytes aus dem BufferedInputStream
        bufferedInput.mark(500); // maximal 500 Bytes werden vorab gelesen
        byte[] trailingBytes = new byte[1000];
        int trailingLength = bufferedInput.read(trailingBytes, 0, 500);
        bufferedInput.reset();
        // wir benötigen die maximale Länge für die nachfolgenden Such-Vorgänge..
        final int maxLength;
        if (trailingLength < 0) {
            maxLength = trailingBytes.length;
        } else {
            maxLength = Math.min(trailingLength, trailingBytes.length);
        }
        // existiert der Prefix im Header?
        int offsetEncodingPrefix = byteIndexOf(trailingBytes, trailingLength, ENCODING_PREFIX);
        // den Fall offsetEncodingPrefix==0 ignorieren wir, er passt nicht zu einer XML-Datei
        final int posAfterPrefix = offsetEncodingPrefix + ENCODING_PREFIX.length;
        if (offsetEncodingPrefix > 0 && posAfterPrefix < maxLength) {
            int endOfCharset = -1;
            for (int srcOffset = posAfterPrefix; srcOffset < maxLength; srcOffset++) {
                if (trailingBytes[srcOffset] == END_ENCODING) {
                    endOfCharset = srcOffset;
                    break;
                }
            }
            if (endOfCharset > offsetEncodingPrefix) { // Wie oben ignorieren wir den Fall endOfCharset==0
                String csName = new String(trailingBytes, posAfterPrefix, endOfCharset - posAfterPrefix, StandardCharsets.US_ASCII);
                try {
                    Charset cs = Charset.forName(csName);
                    return cs.name();
                } catch (UnsupportedCharsetException e) {
                    logger.error("Aus der Datei gelesenes Charset ({}) wird nicht unterstützt", csName, e);
                }
            }
        }
        return CHARSET_UTF8.name();
    }

    protected int byteIndexOf(byte[] sourceBytes, int maxLength, byte[] bytesToFind) {
        for (int srcOffset = 0; srcOffset < maxLength - bytesToFind.length + 1; srcOffset++) {
            boolean found = true;
            for (int btfOffset = 0; btfOffset < bytesToFind.length; btfOffset++) {
                if (sourceBytes[srcOffset + btfOffset] != bytesToFind[btfOffset]) {
                    found = false;
                    break;
                }
            }
            if (found) return srcOffset;
        }
        return -1;
    }

    /**
     * Text-Ersetzung auf String-Basis. Rückgabewert ist ein {@link StringBuilder}
     *
     * @param inString Quell-String
     * @return StringBuilder mit den generierten Daten
     */
    public StringBuilder replace(String keyOriginalMap, String inString) {
        return replaceInternal(keyOriginalMap, patternMap, inString);
    }

    protected StringBuilder replaceInternal(String keyOriginalMap, Map<Pattern, BiFunction<String, String, String>> patternMap, String inString) {
        StringBuilder builder = new StringBuilder(inString);
        for (Map.Entry<Pattern, BiFunction<String, String, String>> e : patternMap.entrySet()) {
            builder = replaceInternal(keyOriginalMap, e.getKey(), e.getValue(), builder.toString());
        }
        return builder;
    }

    protected StringBuilder replaceInternal(String keyOriginalMap, Pattern pattern, BiFunction<String, String, String> replacerFunction, String inString) {
        // logger.debug("Replacer::replaceInternal():\n\tPattern:{}\n\tkeyOriginalMap={}\n\tinString={}", pattern.pattern(), keyOriginalMap, inString);
        StringBuilder sb = new StringBuilder(2 * inString.length());
        Matcher m = pattern.matcher(inString);
        int lastAppendPosition = 0;
        while (m.find()) {
            final String toBeRepaced = m.group();
            //logger.debug("Replacer::replaceInternal():\n\ttoBeRepaced={}", toBeRepaced);
            String replacement = replacerFunction.apply(keyOriginalMap, toBeRepaced); // crefoReplacementMap.get(toBeRepaced);
            if (replacement == null) {
                replacement = toBeRepaced;
            }
            sb.append(inString, lastAppendPosition, m.start());
            sb.append(replacement);
            lastAppendPosition = m.end();
        }
        sb.append(inString.substring(lastAppendPosition));
        //logger.debug("Replacer::replaceInternal():\n\tsb={}", sb);
        return sb;
    }

}
