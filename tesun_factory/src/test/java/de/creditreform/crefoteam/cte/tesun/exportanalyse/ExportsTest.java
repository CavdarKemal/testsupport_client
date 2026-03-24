package de.creditreform.crefoteam.cte.tesun.exportanalyse;

import com.google.common.base.Predicate;
import de.creditreform.crefoteam.ctcbase.sftputil.except.SftpUtilException;
import de.creditreform.crefoteam.ctcbase.sftputil.sftpconnection.SftpConnection;
import de.creditreform.crefoteam.ctcbase.sftputil.sftpconnection.SftpDirectoryEntry;
import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigInfo;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigExportInfo;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigUploadInfo;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestServiceIntegrationTestBase;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.cte.tesun.util.Tupel;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class ExportsTest extends TesunRestServiceIntegrationTestBase {
    private static final String umgebung = "ABE";
    private static final boolean showZipInfos = true;
    private static final File outFileFull = new File(System.getProperty("user.dir"), umgebung + "-CustomerAnalyseInfoFull.txt");
    private static final File outFileOverview = new File(System.getProperty("user.dir"), umgebung + "-CustomerAnalyseInfoOverview.txt");
    List<String> excludeCustomersList = Arrays.asList("INSO");
    private CustomerPredicate<TesunConfigExportInfo> customerPredicate;

    public ExportsTest() {
        super(umgebung);
    }

    @Test
    public void testExtractPrefixAndCrefo() {
        String[] entryNames = new String[]{"deletion_4110184212_4110184211.xml", "stammcrefo_4122187542.xml", "beteiligter_4122188233.xml", "loeschsatz_3450103237.xml", " 4122187541.xml", "9110739399_4122194916.xml"};
        List<Tupel> tupelsList = new ArrayList<>();
        for (String entryName : entryNames) {
            entryName = entryName.replace(".xml", "");
            String prefix = "stammcrefo";
            String strCrefo = "-1";
            String[] splits = entryName.split("[_]");
            for (String split : splits) {
                Matcher matcher = ZipAnalyser.CREFONUMMER_PATTERN.matcher(split);
                if (matcher.matches()) {
                    strCrefo = matcher.group(0);
                } else {
                    matcher = ZipAnalyser.PREFIX_PATTERN.matcher(split);
                    if (matcher.matches()) {
                        prefix = matcher.group(0);
                    }
                }
            }
            Long crefo = Long.parseLong(strCrefo);
            Tupel<String, Long> tupel = new Tupel<>(prefix, crefo);
            logger.info("Entry-Name : " + entryName + " -> Tpel : " + tupel.getElement1() + ":" + tupel.getElement2());
            tupelsList.add(tupel);
        }
    }

    @Test
    public void testCteTestClientPropsConnection() {
        TesunConfigInfo tesunConfigInfo = tesunRestServiceWLS.getTesunConfigInfo();
        List<TesunConfigExportInfo> TesunConfigExportInfoList = tesunConfigInfo.getExportPfade();
        checkSftpConnectionForExportPaths(TesunConfigExportInfoList);
        List<TesunConfigUploadInfo> TesunConfigUploadInfoList = tesunConfigInfo.getUploadPfade();
        checkSftpConnectionForUpladPaths(TesunConfigUploadInfoList);
    }

    private void checkSftpConnectionForExportPaths(List<TesunConfigExportInfo> TesunConfigExportInfoList) {
        System.out.println();
        TesunConfigExportInfoList.stream().forEach(TesunConfigExportInfo -> {
            if (!TesunConfigExportInfo.getNamedAs().contains("junit")) {
                System.out.println("checkSftpConnectionForExportPaths():: " + TesunConfigExportInfo.getKundenKuerzel() + "  " + TesunConfigExportInfo.getNamedAs() + "  " + TesunConfigExportInfo.getRelativePath());
                RestInvokerConfig restInvokerConfig = getGetRestInvokerConfig(TesunConfigExportInfo.getRelativePath());
                try (SftpConnection sftpConnection = SftpConnection
                        .forTrustedHost(restInvokerConfig.getServiceHost(), 22)
                        .setAdditionalAlgo(TestSupportClientKonstanten.ALGORITHM_SSH_RSA)
                        .andUser(restInvokerConfig.getServiceUser(), restInvokerConfig.getServicePassword())) {
                    try {
                        sftpConnection.connect();
                        String[] split = TesunConfigExportInfo.getRelativePath().split("22/");
                        List<SftpDirectoryEntry> sftpDirectoryEntryList = readSftpDirectories(sftpConnection, split[1], "");
                        if (sftpDirectoryEntryList == null) {
                            throw new RuntimeException("!!! SFTP-Fehler! " + TesunConfigExportInfo.getKundenKuerzel() + "  " + TesunConfigExportInfo.getNamedAs() + "  " + TesunConfigExportInfo.getRelativePath());
                        }
                        if (sftpDirectoryEntryList.isEmpty()) {
                            throw new RuntimeException("!!! SFTP-Leer! " + TesunConfigExportInfo.getKundenKuerzel() + "  " + TesunConfigExportInfo.getNamedAs() + "  " + TesunConfigExportInfo.getRelativePath());
                        }
                        /*
                        sftpDirectoryEntryList.stream().forEach(sftpDirectoryEntry -> {
                            String filename = sftpDirectoryEntry.getFilename();
                            long size = sftpDirectoryEntry.getSize();
                            System.out.println("\t" + filename + "     " + size + " Bytes");
                        });
                        */
                    } catch (Exception ex) {
                        System.out.println("\t\t!!!!!!!!!!!!!!!!!!!!!!!!!!: " + ex.getMessage());
                    }
                }
            }
        });
        System.out.println();
    }

    private void checkSftpConnectionForUpladPaths(List<TesunConfigUploadInfo> TesunConfigUploadInfoList) {
        System.out.println();
        TesunConfigUploadInfoList.stream().forEach(TesunConfigUploadInfo -> {
            if(TesunConfigUploadInfo.getKundenKuerzel() == null) {
                System.out.println("\t\t!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
            if (!TesunConfigUploadInfo.getNamedAs().contains("junit")) {
                System.out.println("checkSftpConnectionForUpladPaths():: " + TesunConfigUploadInfo.getKundenKuerzel() + "  " + TesunConfigUploadInfo.getNamedAs() + "  " + TesunConfigUploadInfo.getCompletePath());
                RestInvokerConfig restInvokerConfig = getGetRestInvokerConfig(TesunConfigUploadInfo.getCompletePath());
                try (SftpConnection sftpConnection = SftpConnection
                        .forTrustedHost(restInvokerConfig.getServiceHost(), 22)
                        .setAdditionalAlgo(TestSupportClientKonstanten.ALGORITHM_SSH_RSA)
                        .andUser(restInvokerConfig.getServiceUser(), restInvokerConfig.getServicePassword())) {
                    try {
                        sftpConnection.connect();
                        String[] split = TesunConfigUploadInfo.getCompletePath().split("22/");
                        List<SftpDirectoryEntry> sftpDirectoryEntryList = readSftpDirectories(sftpConnection, split[1], "");
                        if (sftpDirectoryEntryList == null) {
                            throw new RuntimeException("!!! SFTP-Fehler! " + TesunConfigUploadInfo.getKundenKuerzel() + "  " + TesunConfigUploadInfo.getNamedAs() + "  " + TesunConfigUploadInfo.getCompletePath());
                        }
                        if (sftpDirectoryEntryList.isEmpty()) {
                            throw new RuntimeException("!!! SFTP-Leer! " + TesunConfigUploadInfo.getKundenKuerzel() + "  " + TesunConfigUploadInfo.getNamedAs() + "  " + TesunConfigUploadInfo.getCompletePath());
                        }
                        /*
                        sftpDirectoryEntryList.stream().forEach(sftpDirectoryEntry -> {
                            String filename = sftpDirectoryEntry.getFilename();
                            long size = sftpDirectoryEntry.getSize();
                            System.out.println("\t" + filename + "     " + size + " Bytes");
                        });
                        */
                    } catch (Exception ex) {
//                        throw new RuntimeException("!!! Exception! " + ex.getMessage());
                        System.out.println("\t\t!!!!!!!!!!!!!!!!!!!!!!!!!!: " + ex.getMessage());
                    }
                }
            }
        });
        System.out.println();
    }

    private List<SftpDirectoryEntry> readSftpDirectories(SftpConnection sftpConnection, String path, String prefix) throws SftpUtilException {
        List<SftpDirectoryEntry> sftpDirectoryEntryList = sftpConnection.ls(path).stream().filter(sftpDirectoryEntry -> {
            return sftpDirectoryEntry.isDir() && sftpDirectoryEntry.getFilename().startsWith(prefix);
        }).collect(Collectors.toList());
        return sftpDirectoryEntryList;
    }

    private RestInvokerConfig getGetRestInvokerConfig(String thePath) {
        String[] split = thePath.split("@");
        String[] split1 = split[0].split(":");
        String[] split2 = split[1].split("/");
        RestInvokerConfig restInvokerConfig = new RestInvokerConfig(split2[0], split1[0], split1[1]);
        return restInvokerConfig;
    }

    @Test
    public void testExportsSize() {
        outFileFull.delete();
        outFileOverview.delete();
        List<TesunConfigExportInfo> TesunConfigExportInfoList = getExportPfade();
        customerPredicate = new CustomerPredicate(TesunConfigExportInfoList);
        TesunConfigExportInfo TesunConfigExportInfo = TesunConfigExportInfoList.get(0);
        CustormerExportAnalyser custormerExportAnalyser = new CustormerExportAnalyser(TesunConfigExportInfo.getKundenKuerzel(), TesunConfigExportInfo, logger);
        CustomerAnalyseInfo customerAnalyseInfo = custormerExportAnalyser.analyse();
        dumpAnalyseMap(customerAnalyseInfo);
    }

    private void dumpAnalyseMap(CustomerAnalyseInfo customerAnalyseInfo) {
        try {
            String strTemp = "Kunde '" + customerAnalyseInfo.getKundenKuerzel();
            logger.info(strTemp);
            FileUtils.writeStringToFile(outFileFull, strTemp + "\n", true);
            FileUtils.writeStringToFile(outFileOverview, strTemp + "\n", true);
            customerAnalyseInfo.getExportAnalyseInfoList().stream().forEach(exportAnalyseInfo -> {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("\t" + customerAnalyseInfo.getKundenKuerzel() + "-Export: '").append(exportAnalyseInfo.getExportName())
                        .append("',  Größe: ").append(toNumInUnits(exportAnalyseInfo.getExportSize()))
                        .append(", Anzahl ZIP's: ").append(exportAnalyseInfo.getZipAnalyseInfos().size())
                        .append(", ");
                exportAnalyseInfo.getPrefixNumCrefosMap().entrySet().stream().forEach(entry -> {
                    stringBuilder.append("Anzahl ").append(entry.getKey()).append(" : ").append(entry.getValue()).append(", ");
                });
                logger.info(stringBuilder.toString());
                try {
                    FileUtils.writeStringToFile(outFileFull, stringBuilder + "\n", true);
                    FileUtils.writeStringToFile(outFileOverview, stringBuilder + "\n", true);
                    if (showZipInfos) {
                        exportAnalyseInfo.getZipAnalyseInfos().stream().forEach(zipAnalyseInfo -> {
                            if (!zipAnalyseInfo.getZipFilename().contains("abFTN")) {
                                stringBuilder.setLength(0);
                                stringBuilder.append("\t\tZIP-File: " + zipAnalyseInfo.getZipFilename() + " --> ");
                                zipAnalyseInfo.getPrefixCrefosMap().entrySet().stream().forEach(stringListEntry -> {
                                    List<Long> crefosList = stringListEntry.getValue();
                                    stringBuilder.append(" Anzahl ").append(stringListEntry.getKey()).append(" : ").append(crefosList.size()).append(", ");
                                });
                                logger.info(stringBuilder.toString());
                                try {
                                    FileUtils.writeStringToFile(outFileFull, stringBuilder + "\n", true);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static String toNumInUnits(long bytes) {
        int u = 0;
        for (; bytes > 1024 * 1024; bytes >>= 10) {
            u++;
        }
        if (bytes > 1024)
            u++;
        return String.format("%.1f %cB", bytes / 1024f, " kMGTPE".charAt(u));
    }

    private List<TesunConfigExportInfo> getExportPfade() {
        if (umgebung != "PRE") {
            List<TesunConfigExportInfo> TesunConfigExportInfoListX = new ArrayList<>();
            List<TesunConfigExportInfo> TesunConfigExportInfoList = tesunRestServiceWLS.getTesunConfigInfo().getExportPfade();
            TesunConfigExportInfoList.stream().forEach(TesunConfigExportInfo -> {
                if (TesunConfigExportInfo.getKundenKuerzel() != null) {
                    if (!excludeCustomersList.contains(TesunConfigExportInfo.getKundenKuerzel().toUpperCase())) {
                        logger.info(TesunConfigExportInfo.getKundenKuerzel() + ",");
                        TesunConfigExportInfoListX.add(TesunConfigExportInfo);
                    }
                }
            });
            logger.info("\n");
            return TesunConfigExportInfoListX;
        } else {
            List<TesunConfigExportInfo> TesunConfigExportInfoList = new ArrayList<>();
            //List<String> theList = Arrays.asList("BDR", "BIC", "BVD", "CEF", "CRM", "CTC", "DFO", "DRD", "EH", "FSU", "FW", "GDL", "IKA", "INSO", "ISM", "MIC", "MIP", "NIM", "PPA", "RTN", "VSH", "VSD", "VSO", "ZEW");
            List<String> theList = Arrays.asList("BDR", "BVD", "CRM", "CEF", "CTC", "DFO", "DRD", "EH", "FSU", "FW", "FOO", "IKA", "MIC", "MIP", "NIM", "PPA", "RTN", "VSH", "VSD", "VSO", "ZEW");
            theList.stream().forEach(customerKey -> {
                TesunConfigExportInfo TesunConfigExportInfoForPRE = createTesunConfigExportInfoForPRE(customerKey);
                TesunConfigExportInfoList.add(TesunConfigExportInfoForPRE);
            });
            return TesunConfigExportInfoList;
        }
    }

    private static TesunConfigExportInfo createTesunConfigExportInfoForPRE(String customerKey) {
        String url = "seread:*********@fileserver.pre.creditreform.de:22/";
        TesunConfigExportInfo TesunConfigExportInfo = new TesunConfigExportInfo();
        TesunConfigExportInfo.setKundenKuerzel(customerKey.toLowerCase());
        if (customerKey.equalsIgnoreCase("bdr")) {
            customerKey = "bedirect";
        } else if (customerKey.equalsIgnoreCase("ika")) {
            customerKey = "ikaros";
        } else if (customerKey.equalsIgnoreCase("crm")) {
            customerKey = "k26";
        } else if (customerKey.equalsIgnoreCase("eh")) {
            customerKey = "k25";
        }
        TesunConfigExportInfo.setRelativePath(url + customerKey.toLowerCase() + "/export/delta");
        return TesunConfigExportInfo;
    }

    private class CustomerPredicate<TesunConfigExportInfo> implements Predicate<TesunConfigExportInfo> {
        private final List<TesunConfigExportInfo> TesunConfigExportInfoList;

        public CustomerPredicate(List<TesunConfigExportInfo> TesunConfigExportInfoList) {
            this.TesunConfigExportInfoList = TesunConfigExportInfoList;
        }

        public List<TesunConfigExportInfo> getTesunConfigExportInfoList() {
            return TesunConfigExportInfoList;
        }

        @Override
        public boolean apply(TesunConfigExportInfo TesunConfigExportInfo) {
            return TesunConfigExportInfoList.contains(TesunConfigExportInfo);
        }
    }

}


