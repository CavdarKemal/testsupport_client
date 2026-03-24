package de.creditreform.crefoteam.cte.tesun.bic_import_ident;

import de.creditreform.crefoteam.ctcbase.sftputil.sftpconnection.SftpDirectoryEntry;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestServiceIntegrationTestBase;
import de.creditreform.crefoteam.cte.tesun.util.PropertiesException;
import org.junit.Test;

import java.util.Map;

public class ExportsCheckerTest extends TesunRestServiceIntegrationTestBase {

    public ExportsCheckerTest() {
        super("ENE");
    }

    @Test
    public void testCheckExports() throws PropertiesException {
        String exportPath = environmentConfig.getDhlExportPrefix(); // "lokale_exporte/bic/export/delta"; //
        ExportSchecker cut = new ExportSchecker(environmentConfig.getDhlExportSftpHost(), tesunClientJobListener);
        Map<String, SftpDirectoryEntry> todaysExportsEntries = cut.readTodaysExports(exportPath);
        cut.checkNewExports(todaysExportsEntries, exportPath, 0, false);
    }
}
