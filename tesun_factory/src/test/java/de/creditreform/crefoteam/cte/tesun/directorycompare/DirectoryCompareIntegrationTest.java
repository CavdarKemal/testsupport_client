package de.creditreform.crefoteam.cte.tesun.directorycompare;

import de.creditreform.crefoteam.cte.tesun.TestFallTestBase;
import de.creditreform.crefoteam.cte.tesun.util.ClientJobStarter;
import org.junit.Test;

/**
 * Integrations-Tests für {@link DirectoryCompare}
 * User: ralf
 * Date: 26.06.14
 * Time: 13:49
 */
public class DirectoryCompareIntegrationTest extends TestFallTestBase
{
  @Test
  public void testCompareVSDExporte() throws Exception
  {
    setupUtil.configureLog4JProperties();
    setupUtil.prepareForCompareVSDExporte();

    DirectoryCompare clientJob = new DirectoryCompare( setupUtil.getTesunClientJobListener() );
    ClientJobStarter jobStarter = new ClientJobStarter( clientJob );
    startJobTest( jobStarter);
  }
}
