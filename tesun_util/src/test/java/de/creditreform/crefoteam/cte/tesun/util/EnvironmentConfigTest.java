package de.creditreform.crefoteam.cte.tesun.util;

import java.io.File;
import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.*;

public class EnvironmentConfigTest {

    @Test
    public void testGetJobInfoFor() throws Exception {
        EnvironmentConfig cut = new EnvironmentConfig("ENE");

        doTestGetJobInfoFor(cut.getJobInfoForImportCycle(), "importcycle.importCycle", Arrays.asList("BETEILIGUNGEN_IMPORT", "ENTSCHEIDUNGSTRAEGER_BERECHNUNG", "BTLG_UPDATE_TRIGGER", "FROM_STAGING_INTO_CTE"));
        doTestGetJobInfoFor(cut.getJobInfoForBtlgImport(), "importcycle.beteiligungenImportDelta", Arrays.asList("BETEILIGUNGEN_IMPORT"));
        doTestGetJobInfoFor(cut.getJobInfoForEntgBerechnung(), "importcycle.entgBerechnung", Arrays.asList("ENTSCHEIDUNGSTRAEGER_BERECHNUNG"));
        doTestGetJobInfoFor(cut.getJobInfoForBtlgAktualisierung(), "importcycle.btlnAktualisierung", Arrays.asList("BTLG_UPDATE_TRIGGER"));
        doTestGetJobInfoFor(cut.getJobInfoForCtImport(), "importcycle.ctImportDelta", Arrays.asList("FROM_STAGING_INTO_CTE"));
    }

    protected void doTestGetJobInfoFor(JobInfo jobInfoFor, String expectedJobName, List<String> expectedProcessNamesList) {
        Assert.assertNotNull(jobInfoFor);
        Assert.assertEquals("Jobname stimmt nicht!", expectedJobName, jobInfoFor.getJobName());
        List<String> processNamesList = jobInfoFor.getProcessNamesList();
        Assert.assertEquals("Anzahl Prozesse stimmt nicht!", expectedProcessNamesList.size(), processNamesList.size());
        int index = 0;
        for (String strTemp : expectedProcessNamesList) {
            Assert.assertEquals("Prozessname stimmt nicht!", strTemp, processNamesList.get(index++));
        }
    }

    @Test
    public void testGetItsqTagNameFormat() throws Exception {
        EnvironmentConfig cut = new EnvironmentConfig("ENE");
        Date dateNow = Calendar.getInstance().getTime();
        SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH.mm.ss", Locale.getDefault());

        String itsqTagNameFormat = cut.getItsqTagNameFormat(); // "0;ENE;tesuntestene;08.04.2024;09.03.10;null;IntialTest"

        String initialTest = "IntialTest";
        String itsqTagName = cut.getItsqTagName(itsqTagNameFormat, initialTest); // cavdark-ENE-17.10.2018-17.42.15-INITIAL_TEST
        Assert.assertTrue("ItsqTagName sollte CTE-Version enthalten!", itsqTagName.contains(cut.getCteVersion()+""));
        Assert.assertTrue("ItsqTagName sollte Repository-User enthalten!", itsqTagName.contains(cut.getRepositoryUserName()));
        Assert.assertTrue("ItsqTagName sollte Environment enthalten!", itsqTagName.contains(cut.getCurrentEnvName()));
        Assert.assertTrue("ItsqTagName sollte Datum enthalten!", itsqTagName.contains(DATE_FORMATTER.format(dateNow)));
        Assert.assertTrue("ItsqTagName sollte Zeit enthalten!", itsqTagName.contains(TIME_FORMATTER.format(dateNow)));
        Assert.assertTrue("ItsqTagName sollte Test-Typ enthalten!", itsqTagName.contains(initialTest));

        String repeatableTest = "RepeatableTest";
        itsqTagName = cut.getItsqTagName(itsqTagNameFormat, repeatableTest); // cavdark-ENE-17.10.2018-17.42.15-REPEATABLE_TEST
        Assert.assertTrue("ItsqTagName sollte Test-Typ enthalten!", itsqTagName.contains(repeatableTest));

        itsqTagNameFormat = "ENV-DATE-TIME";
        itsqTagName = cut.getItsqTagName(itsqTagNameFormat, repeatableTest);
        Assert.assertFalse("ItsqTagName dürfte Repository-User Nicht enthalten!", itsqTagName.startsWith(cut.getRepositoryUserName()));
        Assert.assertTrue("ItsqTagName sollte Environment enthalten!", itsqTagName.contains(cut.getCurrentEnvName()));
        Assert.assertTrue("ItsqTagName sollte Datum enthalten!", itsqTagName.contains(DATE_FORMATTER.format(dateNow)));
        Assert.assertTrue("ItsqTagName sollte Zeit enthalten!", itsqTagName.contains(TIME_FORMATTER.format(dateNow)));
        Assert.assertTrue("ItsqTagName sollte Test-Typ enthalten!", itsqTagName.contains(repeatableTest));

        itsqTagNameFormat = "DATE-TIME";
        itsqTagName = cut.getItsqTagName(itsqTagNameFormat, repeatableTest);
        Assert.assertFalse("ItsqTagName dürfte Repository-User Nicht enthalten!", itsqTagName.startsWith(cut.getRepositoryUserName()));
        Assert.assertFalse("ItsqTagName sollte Environment enthalten!", itsqTagName.contains(cut.getCurrentEnvName()));
        Assert.assertTrue("ItsqTagName sollte Datum enthalten!", itsqTagName.contains(DATE_FORMATTER.format(dateNow)));
        Assert.assertTrue("ItsqTagName sollte Zeit enthalten!", itsqTagName.contains(TIME_FORMATTER.format(dateNow)));
        Assert.assertTrue("ItsqTagName sollte Test-Typ enthalten!", itsqTagName.contains(repeatableTest));

        itsqTagNameFormat = "USER-ENV-TIME";
        itsqTagName = cut.getItsqTagName(itsqTagNameFormat, repeatableTest);
        Assert.assertTrue("ItsqTagName sollte Repository-User enthalten!", itsqTagName.startsWith(cut.getRepositoryUserName()));
        Assert.assertTrue("ItsqTagName sollte Environment enthalten!", itsqTagName.contains(cut.getCurrentEnvName()));
        Assert.assertFalse("ItsqTagName sollte Datum enthalten!", itsqTagName.contains(DATE_FORMATTER.format(dateNow)));
        Assert.assertTrue("ItsqTagName sollte Zeit enthalten!", itsqTagName.contains(TIME_FORMATTER.format(dateNow)));
        Assert.assertTrue("ItsqTagName sollte Test-Typ enthalten!", itsqTagName.contains(repeatableTest));
    }

    @Test
    public void testPaths() throws PropertiesException {
        EnvironmentConfig cut = new EnvironmentConfig("ENE");
        String currentEnvName = cut.getCurrentEnvName();
        Assert.assertEquals("Test-Umgebung stimmt nicht", "ENE", currentEnvName);
        File workDir = cut.getTestResourcesRoot();
        Assert.assertTrue("Arbeitsverzeichnis stimmt nicht!", workDir.getName().equalsIgnoreCase("test-classes"));
        File testBaseDir = cut.getTestResourcesRoot();
        Assert.assertTrue("Basis-Test-Verzeichnis stimmt nicht!", testBaseDir.getName().equalsIgnoreCase("ENE"));
        File configFile = cut.getEnvironmentConfigFile();
        Assert.assertTrue("Config-Dateiname stimmt nicht!", configFile.getName().equalsIgnoreCase("ENE-config.properties"));

    }
}
