package de.creditreform.crefoteam.cte.tesun.util;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.directorytree.FileActionFactoryCollectEntries;
import de.creditreform.crefoteam.cte.tesun.util.directorytree.TreeProcessor;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.log4j.Level;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class TesunUtilites {

    public static String shortPath(String thePath, int maxLen) {
        return shortPath(new File(thePath), maxLen);
    }

    public static void sendEmail(String smtpHost, int smtpPort, String emailFrom, String emailTo, String emailSubject, String emailContent, String attachmentFileName) throws EmailException {
        MultiPartEmail multiPartEmail = new MultiPartEmail();
        multiPartEmail.setHostName(smtpHost);
        multiPartEmail.setSmtpPort(smtpPort);
        multiPartEmail.setFrom(emailFrom);
        String[] split = emailTo.split("[,;]");
        for (String rcv : split) {
            multiPartEmail.addTo(rcv);
        }
        multiPartEmail.setSubject(emailSubject);
        if (emailContent.isBlank()) {
            emailContent = "Ohne Content!";
        }
        multiPartEmail.setMsg(emailContent);
        if (attachmentFileName != null && !attachmentFileName.isEmpty()) {
            EmailAttachment attachment = new EmailAttachment();
            attachment.setPath(attachmentFileName);
            attachment.setDisposition(EmailAttachment.ATTACHMENT);
            attachment.setDescription("Das Verzeichnis TEST_OUTPUTS komprimiert");
            attachment.setName(attachmentFileName);
            multiPartEmail.attach(attachment);
        }
        multiPartEmail.send();
    }

    public static String formatMap(Map<String, List<Long>> theMap, int maxLen) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, List<Long>> entry : theMap.entrySet()) {
            stringBuilder.append(entry.getKey()).append(": [\t");
            final List<Long> longList = entry.getValue();
            for (int i = 0; i < longList.size(); i++) {
                stringBuilder.append(longList.get(i));
                if ((i + 1) % maxLen == 0) {
                    stringBuilder.append("\n\t\t");
                } else if (i < longList.size() - 1) {
                    stringBuilder.append(", ");
                }
            }
            stringBuilder.append(" ]\n\n");
        }
        return stringBuilder.toString();
    }


    public static String shortPath(File theFile, int maxLen) {
        String absolutePath = theFile.getAbsolutePath();
        if (absolutePath.length() < maxLen) {
            return theFile.getAbsolutePath();
        }
        File rootFile = new File(absolutePath);
        while (rootFile.getParentFile().getParentFile() != null) {
            rootFile = rootFile.getParentFile();
        }
        int rootLen = rootFile.getAbsolutePath().length();
        while (absolutePath.length() >= (rootLen + maxLen)) {
            int fistSlashPos = absolutePath.indexOf(File.separator, 3);
            if (fistSlashPos < 0) {
                break;
            }
            absolutePath = absolutePath.substring(fistSlashPos);
        }
        if (!absolutePath.startsWith(rootFile.getPath())) {
            absolutePath = rootFile + File.separator + "..." + absolutePath;
        }
        return absolutePath;
    }

    public static List<File> getFilesFromDir(File theRoot, final String regExp) throws IOException {
        File[] files = null;
        if (theRoot.exists()) {
            files = theRoot.listFiles((dir, fileName) -> {
                boolean matches = fileName.endsWith(regExp);
                if (!matches) {
                    matches = fileName.matches(regExp);
                }
                return matches;
            });
        }
        // Damit die Liste später im Code modifizierbar ist, hier als ArrayList aufbauen!
        List<File> filesFromDir = new ArrayList<>();
        if (files != null) {
            Collections.addAll(filesFromDir, files);
        }
        // Sortieren..
        Collections.sort(filesFromDir, (o1, o2) -> o1.getPath().compareTo(o2.getPath()));
        return filesFromDir;
    }

    public static String buildExceptionMessage(Throwable ex, int maxLines) {
        String className = "";
        String errMsg = "";
        if (ex != null) {
            className = ex.getClass().getName();
            StringWriter writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            printWriter.flush();
            errMsg = writer.toString();
        }
        if (errMsg.startsWith("null") || errMsg.isBlank()) {
            while (ex != null) {
                errMsg += ex.getMessage();
                errMsg += "\n\t";
                ex = ex.getCause();
            }
        }
        if (errMsg.startsWith("null") || errMsg.isBlank()) {
            errMsg = className;
        }
        return errMsg;
    }

    public static String checkAndCreateDirectoryX(File theFile) {
        if (!theFile.exists()) {
            boolean mkdirs = theFile.mkdirs();
            if (!mkdirs) {
                String strErr = String.format("Das Verzeichnis %s konnte nicht angelegt werden!", theFile.getAbsolutePath());
                throw new RuntimeException(strErr);
            }
        }
        return theFile.getAbsolutePath();
    }

    public static String checkAndCreateDirectory(File phaseFile, boolean create) throws IOException {
        if (create) {
            if (phaseFile.exists()) {
                File destDir = new File(phaseFile + "-" + System.nanoTime());
                FileUtils.moveDirectory(phaseFile, destDir);
            }
            boolean mkdirs = phaseFile.mkdirs();
            if (!mkdirs) {
                String strErr = String.format("Das Verzeichnis %s konnte nicht angelegt werden!", phaseFile.getAbsolutePath());
                throw new RuntimeException(strErr);
            }
        } else if (!phaseFile.exists()) {
            String strErr = String.format("Das Verzeichnis %s existiert nicht!", phaseFile.getAbsolutePath());
            throw new RuntimeException(strErr);
        }
        return phaseFile.getAbsolutePath();
    }

    public static List<File> getErrorFilesFor(File srcDir) throws Exception {
        FileActionFactoryCollectEntries collectingFactory = new FileActionFactoryCollectEntries();
        TreeProcessor treeProcessor = new TreeProcessor(theFile -> {
            return theFile.isDirectory() || theFile.getName().endsWith(TestSupportClientKonstanten.ERRORS_TXT);
        }, srcDir, collectingFactory);
        treeProcessor.call();
        return collectingFactory.getFiles();
    }

    public static List<File> handleCustomersFiles(Map<String, TestCustomer> testCustomersMap, String dstPath, String[] extensionsList, boolean throwException) throws Exception {
        List<File> copiedFilesList = new ArrayList<>();
        for (Map.Entry<String, TestCustomer> testCustomerEntry : testCustomersMap.entrySet()) {
            final TestCustomer testCustomer = testCustomerEntry.getValue();
            for (String extension : extensionsList) {
                File targetDir = new File(dstPath, testCustomer.getCustomerKey().toLowerCase());
                List<File> copiedFilesForCustomer = copyFilesForCustomer(testCustomer, targetDir, extension);
                if (throwException && copiedFilesForCustomer.size() < 1) {
                    String strInfo = String.format("Das Verzeichnis \n\t%s\nenthält keine %s- Dateien!", testCustomer.getItsqRefExportsDir(), extension);
                    throw new FileNotFoundException(strInfo);
                } else {
                    copiedFilesList.addAll(copiedFilesForCustomer);
                }
            }
        }
        return copiedFilesList;
    }

    public static List<File> copyFilesForCustomer(final TestCustomer testCustomer, final File customerTargetDir, final String extension) throws IOException {
        final List<File> copiedFilesList = new ArrayList<>();
        FileUtils.copyDirectory(testCustomer.getItsqRefExportsDir(), customerTargetDir, theFile -> {
            String name = theFile.getName();
            if (theFile.isDirectory()) {
                TestScenario scenario = testCustomer.getScenario(name);
                if (scenario != null) {
                    return scenario.isActivated();
                }
                return false;
            } else {
                boolean endsWith = name.endsWith(extension);
                if (endsWith) {
                    copiedFilesList.add(theFile);
                }
                return endsWith;
            }
        });
        return copiedFilesList;
    }

    public static String toPrettyString(String xml, int indent) {
        String charsetName = "UTF-8";
        try {
            final InputSource inputSource = new InputSource(new ByteArrayInputStream(xml.getBytes(charsetName)));
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputSource);
            // Remove whitespaces outside tags
            document.normalize();
            return toPrettyString(document, indent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    public static String toPrettyString(Document document, int indent) throws Exception {
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList nodeList = (NodeList) xPath.evaluate("//text()[normalize-space()='']", document, XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); ++i) {
            Node node = nodeList.item(i);
            node.getParentNode().removeChild(node);
        }
        // Setup pretty print options
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute("indent-number", indent);
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        // Return pretty print xml string
        StringWriter stringWriter = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
        return stringWriter.toString();
    }

    public static void formatXMLFilesInDir(File srcDir, File dstDir) throws IOException {
        Files.walk(srcDir.toPath())
                .filter(path -> !Files.isDirectory(path))
                .forEach(path -> {
                    File xmlFile = path.toFile();
                    String xmlFileName = xmlFile.getName();
                    if (!xmlFileName.endsWith(".xml")) {
                        return;
                    }
                    try {
                        String xmlContent = FileUtils.readFileToString(xmlFile);
                        String newXmlFileName = xmlFile.getAbsolutePath().replace(srcDir.getPath(), "");
                        File outputFile = new File(dstDir, newXmlFileName);
                        if (!outputFile.getParentFile().exists()) {
                            outputFile.getParentFile().mkdirs();
                        }
                        final String formattedXMLContent = TesunUtilites.toPrettyString(xmlContent, 2);
                        FileUtils.writeStringToFile(outputFile, formattedXMLContent, StandardCharsets.UTF_8);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
    }

    public static Long extractCrefonummerFromString(String strErr) {
        Matcher matcher = TestSupportClientKonstanten.CREFONUMMER_PATTERN.matcher(strErr);
        if (matcher.find()) {
            return Long.valueOf(matcher.group(0));
        }
        return null;
    }

    public static void waitForFutureTasks(Map<String, FutureTask> callablesMap, TesunClientJobListener tesunClientJobListener) throws InterruptedException, ExecutionException {
        long nCnt = 0;
        while (true) {
            Iterator<String> iterator = callablesMap.keySet().iterator();
            if (!iterator.hasNext()) {
                break;
            }
            String key = iterator.next();
            FutureTask futureTask = callablesMap.get(key);
            if (futureTask.isDone()) {
                futureTask.get();
                callablesMap.remove(key);
            }
            if (++nCnt % 10000000 == 0) {
                tesunClientJobListener.notifyClientJob(Level.INFO, ".");
            }
        }
    }

    public static void dumpCustomers(File logsDir, String prefix, Map<String, TestCustomer> customersMap) throws IOException {
        final StringBuilder stringBuilder = new StringBuilder(prefix);
        customersMap.entrySet().forEach((Map.Entry<String, TestCustomer> testPhaseMapEntry) -> {
            TestCustomer testCustomer = testPhaseMapEntry.getValue();
            if (testCustomer.isActivated()) {
                stringBuilder.append("\n\t\t").append(testCustomer.getCustomerKey());
                stringBuilder.append("\n\t\tJVM-Name:").append(testCustomer.getJvmName());
                stringBuilder.append("\n\t\t\tProcess-Identifier:").append(testCustomer.getProcessIdentifier());
                stringBuilder.append("\n\t\t\tExport-Jobname:").append(testCustomer.getExportJobName());
                stringBuilder.append("\n\t\t\tExport-URL:").append(testCustomer.getExportUrl());
                stringBuilder.append("\n\t\t\tUpload-Jobname:").append(testCustomer.getUploadJobName());
                stringBuilder.append("\n\t\t\tUpload-URL:").append(testCustomer.getUploadUrl());
                stringBuilder.append("\n\t\t\tITSQ-AB30Xmls-Dir:").append(testCustomer.getItsqAB30XmlsDir());
                stringBuilder.append("\n\t\t\tITSQ-RefExports-Dir:").append(testCustomer.getItsqRefExportsDir());
                stringBuilder.append("\n\t\t\tPseudo-RefExports-Dir:").append(testCustomer.getPseudoRefExportsDir());
                stringBuilder.append("\n\t\t\tCollects-Dir:").append(testCustomer.getCollectedsDir());
                stringBuilder.append("\n\t\t\tRestored-Collects-Dir:").append(testCustomer.getRestoredCollectedsDir());
                stringBuilder.append("\n\t\t\tChecks-Dir:").append(testCustomer.getChecksDir());
                stringBuilder.append("\n\t\t\tSFTP-Uploads-Dir:").append(testCustomer.getSftpUploadsDir());
                stringBuilder.append("\n\t\t\tFW-Aktualisierung:").append(testCustomer.getFwAktualisierungsdatum());
                stringBuilder.append("\n\t\t\tPD-Version:").append(testCustomer.getPdVersion());

                stringBuilder.append("\n\t\t\tProperties:");
                testCustomer.getPropertyPairsList().forEach(propertyPair -> {
                    stringBuilder.append("\n\t\t\t\t").append(propertyPair.getLeft()).append("=").append(propertyPair.getRight());
                });
            }
        });
        File file = new File(logsDir, ("Dump-" + prefix + ".txt"));
        FileUtils.writeStringToFile(file,stringBuilder.toString());
    }
}
