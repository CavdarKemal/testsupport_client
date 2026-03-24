package de.creditreform.crefoteam.cte.tesun.exports_collector;

import de.creditreform.crefoteam.cte.pathabstraction.api.PathElement;
import de.creditreform.crefoteam.cte.pathabstraction.fileimpl.PathElementFileSystem;
import de.creditreform.crefoteam.cte.tesun.util.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ExportsAdapterExtractAndSaveZipFilesTest extends ExportsAdapterTestBase {

    @Test
    public void testExtractAndSaveZipInputStream() throws Exception {
        List<TestCrefo> testCrefoList = new ArrayList<>();
        testCrefoList.add(new TestCrefo("p01", 4123529621L)); // P-Test mit Beteiligten-Export: beteiligter_4123529621.xml -> OK
        testCrefoList.add(new TestCrefo("p02", 4123529490L)); // P-Test mit Stammsatz-Export: stammcrefo_4123529490.xml -> OK
        testCrefoList.add(new TestCrefo("p03", 4123525226L)); // P-Test mit Löschsatz-Export: loeschsatz_4123525226.xml -> Fehler
        testCrefoList.add(new TestCrefo("p04", 4129999990L)); // P-Test ohne Export -> Fehler

        testCrefoList.add(new TestCrefo("n03", 4129999992L)); // N-Test ohne Export -> OK
        testCrefoList.add(new TestCrefo("n01", 4123442689L)); // N-Test mit Löschsatz-Export: loeschsatz_4123442689.xml -> Fehler
        testCrefoList.add(new TestCrefo("n02", 4123529724L)); // N-Test mit Stammsatz-Export: stammcrefo_4123529724.xml -> Fehler

        testCrefoList.add(new TestCrefo("x01", 4123525220L)); // X-Test mit Löschsatz-Export: loeschsatz_4123525220.xml -> OK
        testCrefoList.add(new TestCrefo("x02", 4123525204L)); // X-Test mit Löschsatz-Export: stammcrefo_4123525204.xml -> Fehler
        testCrefoList.add(new TestCrefo("x03", 4129999992L)); // X-Test ohne Export -> Fehler

        TestCustomer testCustomer = customerTestInfoMap.get("BVD");
        TestScenario testScenario = fillTestCrefosForTesScenario(testCrefoList, testCustomer);
        ExportsAdapterDefImpl cut = getExportsAdapter(testCustomer.getCustomerKey());
        File zipFile = new File(getClass().getResource("/TESTS/abCrefo_4123000000.zip").toURI());
        List<File> fileList = cut.extractAndSaveZipInputStream(testScenario, zipFile);
    }

    @Test
    public void testFindFirstJoungerElement() throws Exception {
        Calendar lastExportCal = TesunDateUtils.extractDateFromString("2025-01-04_11-34");
        List<PathElement > exportDirsList = new ArrayList<>();
        exportDirsList.add(new PathElementFileSystem(new File("delta_2025.10.16_12.44")));
        exportDirsList.add(new PathElementFileSystem(new File("delta_2025-11-32_21-12")));
        exportDirsList.add(new PathElementFileSystem(new File("delta_2025-01-02_04-23")));
        exportDirsList.add(new PathElementFileSystem(new File("delta_2025-02-05_13-44")));
        exportDirsList.add(new PathElementFileSystem(new File("delta_2025-11-32_21-12")));
        exportDirsList.add(new PathElementFileSystem(new File("delta_2025-02-06_22-14")));
        PathElement firstJoungerElement = PathElementUtils.findFirstJoungerElement(tesunClientJobListener, exportDirsList, lastExportCal);
        Assert.assertEquals(firstJoungerElement.getName(), "delta_2025-02-05_13-44");
    }

}
