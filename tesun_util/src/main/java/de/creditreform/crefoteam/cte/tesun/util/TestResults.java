package de.creditreform.crefoteam.cte.tesun.util;

import org.custommonkey.xmlunit.Difference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestResults {
    private final List<ResultInfo> resultInfosList = new ArrayList<>();
    private final String command;

    public TestResults(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public void addResultInfo(ResultInfo resultInfo) {
        resultInfosList.add(resultInfo);
    }

    public List<ResultInfo> getResultInfosList() {
        return resultInfosList;
    }

    public StringBuilder dumpResults(StringBuilder stringBuilder, String prefix) {
        resultInfosList.forEach(resultInfo -> {
            String errMsg = resultInfo.getErrorStr();
            stringBuilder.append(prefix + "\t");
            stringBuilder.append(errMsg);
        });
        return stringBuilder;
    }

    public static class DiffenrenceInfo {
        Map<String, List<Difference>> diffsMap = new HashMap<>();
        final File xmlFileSrc;
        final File xmlFileDst;
        final String testFallName;
        File diffFile;

        public DiffenrenceInfo(String testFallName, File xmlFileSrc, File xmlFileDst, File diffFile, List<Difference> differenceList) {
            this.testFallName = testFallName;
            this.xmlFileSrc = xmlFileSrc;
            this.xmlFileDst = xmlFileDst;
            this.diffFile = diffFile;
            diffsMap.put(diffFile.getName(), differenceList);
        }

        public String getTestFallName() {
            return testFallName;
        }

        public File getXmlFileSrc() {
            return xmlFileSrc;
        }

        public File getXmlFileDst() {
            return xmlFileDst;
        }

        public File getDiffFile() {
            return diffFile;
        }

        public Map<String, List<Difference>> getDiffsMap() {
            return diffsMap;
        }

    }

    public static class ResultInfo {
        List<DiffenrenceInfo> diffenrenceInfosList = new ArrayList<>();

        String errorStr;
        Long crefoNummer;

        public ResultInfo(Long crefoNummer, String errorStr) {
            this.crefoNummer = crefoNummer;
            this.errorStr = errorStr;
        }

        public ResultInfo(String xmlName, String errorStr) {
            this.crefoNummer = TesunUtilites.extractCrefonummerFromString(xmlName);
            this.errorStr = errorStr;
        }

        public ResultInfo(String errorStr) {
            try {
                this.crefoNummer = TesunUtilites.extractCrefonummerFromString(errorStr);
            } catch (Exception ex) {
                // Fehlertext ethält eben keine Crefonummer :-)
                this.crefoNummer = -1L;
            }
            this.errorStr = errorStr;
        }

        public Long getCrefoNummer() {
            return crefoNummer;
        }

        public String getErrorStr() {
            return errorStr;
        }

        public String appendToErrorStr(String strAppend) {
            errorStr += strAppend;
            return errorStr;
        }

        public List<DiffenrenceInfo> getDiffenrenceInfosList() {
            return diffenrenceInfosList;
        }

        public void addDifferences(DiffenrenceInfo diffenrenceInfo) {
            diffenrenceInfosList.add(diffenrenceInfo);
        }
    }
}
