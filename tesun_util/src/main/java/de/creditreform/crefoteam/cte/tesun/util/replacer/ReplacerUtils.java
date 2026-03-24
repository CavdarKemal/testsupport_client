package de.creditreform.crefoteam.cte.tesun.util.replacer;

import de.creditreform.crefoteam.cte.tesun.util.TestCrefo;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestScenario;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ReplacerUtils {
    public static void saveMappingFiles(File outputPath, Map<String, TestCustomer> activeCustomersMap, Map<String, ReplacementMapping> replacementMappingMap) throws IOException {
        List<String> mappingLines = new ArrayList<>();
        Stream<String> sorted = replacementMappingMap.keySet().stream().sorted();
        sorted.forEach(origCrefo -> {
            ReplacementMapping replacementMapping = replacementMappingMap.get(origCrefo);
            String strLine = String.format("%s;%d;%d;%s", origCrefo, replacementMapping.getTargetCrefo(), replacementMapping.getEignerVC(), replacementMapping.getTagNameEignerVC());
            mappingLines.add(strLine);
        });
        FileUtils.writeLines(new File(outputPath, TestSupportClientKonstanten.fileNameCrefosMapping), mappingLines);
        saveNachlieferungsMap(outputPath, activeCustomersMap, replacementMappingMap);
    }

    public static void saveNachlieferungsMap(File outputPath, Map<String, TestCustomer> activeCustomersMap, Map<String, ReplacementMapping> replacementMappingMap) throws IOException {
        File outputFile = new File(outputPath, "Nachlieferung");
        for (Map.Entry<String, TestCustomer> entry : activeCustomersMap.entrySet()) {
            List<String> customerMappingLines = new ArrayList<>();
            TestCustomer testCustomer = entry.getValue();
            for (TestScenario testScenario : testCustomer.getTestScenariosList()) {
                List<String> scenarioMappingLines = new ArrayList<>();
                Map<String, TestCrefo> testFallNameToTestCrefoMap = testScenario.getTestFallNameToTestCrefoMap();
                testFallNameToTestCrefoMap.entrySet().forEach(testCrefoEntry -> {
                    TestCrefo testCrefo = testCrefoEntry.getValue();
                    ReplacementMapping replacementMapping = replacementMappingMap.get(testCrefo.getItsqTestCrefoNr().toString());
                    testCrefo.setPseudoCrefoNr(replacementMapping.getTargetCrefo());
                    scenarioMappingLines.add(replacementMapping.getTargetCrefo().toString());
                });
                String strFileName = testCustomer.getCustomerKey() + "/" + testScenario.getScenarioName() + "." + TestSupportClientKonstanten.fileNameCrefosFuerNachlieferung;
                FileUtils.writeLines(new File(outputFile, strFileName), scenarioMappingLines);
                customerMappingLines.addAll(scenarioMappingLines);
            }
            String strFileName = testCustomer.getCustomerKey() + "/" + TestSupportClientKonstanten.fileNameCrefosFuerNachlieferung;
            FileUtils.writeLines(new File(outputFile, strFileName), customerMappingLines);
        }
    }
}
