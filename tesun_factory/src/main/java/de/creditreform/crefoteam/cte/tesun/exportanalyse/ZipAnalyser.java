package de.creditreform.crefoteam.cte.tesun.exportanalyse;

import de.creditreform.crefoteam.ctcbase.sftputil.except.SftpUtilException;
import de.creditreform.crefoteam.ctcbase.sftputil.sftpconnection.SftpConnection;
import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.cte.tesun.util.Tupel;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipAnalyser {
    public static Pattern CREFONUMMER_PATTERN = Pattern.compile("\\d{10}");
    public static Pattern PREFIX_PATTERN = Pattern.compile("^\\w+");
    private final RestInvokerConfig restInvokerConfig;
    private final CteZipFileEntry cteZipFileEntry;
    private Logger logger;

    public ZipAnalyser(RestInvokerConfig restInvokerConfig, CteZipFileEntry cteZipFileEntry, Logger logger) {
        this.restInvokerConfig = restInvokerConfig;
        this.cteZipFileEntry = cteZipFileEntry;
        this.logger = logger;
    }

    public ZipAnalyseInfo analyseZip() {
        File zipFile = new File(cteZipFileEntry.getFilename());
        return extractFilesFromZip(zipFile);
    }

    public long getZipFileSize() {
        return -1;
    }

    public ZipAnalyseInfo extractFilesFromZip(File zipFile) {
        ZipAnalyseInfo zipAnalyseInfo = new ZipAnalyseInfo(zipFile.getName());
        if (zipFile.getName().contains("abFTN")) {
            return zipAnalyseInfo;
        }
        logger.info("\t\tAnalysiere '" + zipFile.getPath() + "'...");
        int echoPer = restInvokerConfig.getServiceHost().contains("pre") ? 500 : 1;
        try (SftpConnection sftpConnection = SftpConnection
                .forTrustedHost(restInvokerConfig.getServiceHost(), 22)
                .setAdditionalAlgo(TestSupportClientKonstanten.ALGORITHM_SSH_RSA)
                .andUser(restInvokerConfig.getServiceUser(), restInvokerConfig.getServicePassword())) {
            sftpConnection.connect();
            ZipEntry entry;
            InputStream inputStream = sftpConnection.get(zipFile.getPath().replaceAll("\\\\", "/"));
            ZipInputStream zipInputStream = new ZipInputStream(inputStream);
            try {
                int numEntries = 0;
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    if(numEntries++ % echoPer == 0) {
                        //logger.info(".");
                    }
                    Tupel<String, Long> prefixAndCrefoTupel = extractFromEntryName(entry.getName());
                    Map<String, List<Long>> prefixCrefosMap = zipAnalyseInfo.getPrefixCrefosMap();
                    if (!prefixCrefosMap.containsKey(prefixAndCrefoTupel.getElement1())) {
                        prefixCrefosMap.put(prefixAndCrefoTupel.getElement1(), new ArrayList<>());
                    }
                    List<Long> crefosList = prefixCrefosMap.get(prefixAndCrefoTupel.getElement1());
                    crefosList.add(prefixAndCrefoTupel.getElement2());
                }
                logger.info("\tAnzahl Entries : " + numEntries);
            } catch (IOException ex) {
                logger.info("IOException: " + ex.getMessage());
            } finally {
                zipInputStream.close();
            }
        } catch (SftpUtilException | IOException ex) {
            logger.info("SftpUtilException | IOException: " + ex.getMessage());
        }
        return zipAnalyseInfo;
    }

    private Tupel<String, Long> extractFromEntryName(String entryName) {
        try {
            entryName = entryName.replace(".xml", "");
            String prefix = "stammcrefo";
            String strCrefo = "-1";
            String[] splits = entryName.split("[_]");
            for (String split : splits) {
                Matcher matcher = CREFONUMMER_PATTERN.matcher(split);
                if (matcher.matches()) {
                    strCrefo = matcher.group(0);
                } else {
                    matcher = PREFIX_PATTERN.matcher(split);
                    if (matcher.matches()) {
                        prefix = matcher.group(0);
                    }
                }
            }
            Long crefo = Long.parseLong(strCrefo);
            Tupel<String, Long> tupel = new Tupel<>(prefix, crefo);
            return tupel;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
