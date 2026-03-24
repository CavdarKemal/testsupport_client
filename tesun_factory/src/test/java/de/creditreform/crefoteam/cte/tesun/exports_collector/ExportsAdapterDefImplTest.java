package de.creditreform.crefoteam.cte.tesun.exports_collector;

import de.creditreform.crefoteam.cte.tesun.testutil.TesunTestSetupUtil;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.technischebasis.log4j.SystemOutAppender;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by CavdarK on 19.07.2016.
 */
public class ExportsAdapterDefImplTest {

   protected static final Logger logger = LoggerFactory.getLogger(ExportsAdapterDefImplTest.class);
   protected static TesunTestSetupUtil setupUtil;
   protected static Map<String, TestCustomer> customerTestInfoMap;

   @BeforeClass
   public static void setUp() {
      try {
         SystemOutAppender.INFO().installIntoRootLogger();
         setupUtil = new TesunTestSetupUtil();
         setupUtil.setUp();
         setupUtil.configureLog4JProperties();
         customerTestInfoMap = setupUtil.readTestCustomersMap(TestSupportClientKonstanten.TEST_PHASE.PHASE_1);
      } catch (Exception ex) {
         Assert.fail(ex.getMessage());
      }
   }
   private static String envKey = "ENE";

/*
   protected ByteArrayOutputStream retrieveCompletitionInfoFile(ExportsAdapter exportsAdapter, Date fromDate) throws Exception {
      logger.debug("retrieveCompletitionInfoFile():: Kunde: " + exportsAdapter.getCustomerKey());
      ByteArrayOutputStream completitionFileBAOS = exportsAdapter.retrieveCompletitionInfoFile(fromDate);
      return completitionFileBAOS;
   }

   protected List<PathElement> listDirPathElements(ExportsAdapter exportsAdapter) throws Exception {
      logger.debug("listDirPathElements():: Kunde: " + exportsAdapter.getCustomerKey());
      List<PathElement> pathElements = exportsAdapter.listDirPathElements();
      Assert.assertNotNull(pathElements);
      return pathElements;
   }

   protected List<PathElement> listZipPathElements(ExportsAdapter exportsAdapter) throws Exception {
      logger.debug("listZipPathElements():: Kunde: " + exportsAdapter.getCustomerKey());
      List<NameCrefo> nameCrefoList = new ArrayList<>();
      List<PathElement> pathElements = exportsAdapter.listZipPathElements( nameCrefoList);
      Assert.assertNotNull(pathElements);
      return pathElements;
   }

   protected ExportsAdapter getExportsAdapter(boolean isLocal, String customerKey) {
      TesunConfigInfo tesunConfigInfo = setupUtil.getTesunConfigInfo(isLocal);
      ExportsAdapterConfig exportsAdapterConfig = new ExportsAdapterConfig(tesunConfigInfo, customerKey);
      ExportsAdapter cut = new ExportsAdapter(exportsAdapterConfig, null);
      Assert.assertNotNull(cut);
      return cut;
   }

   @Test
   public void testExportsAdapter_isJoungestExportDirAfterDate() throws Exception {
      String customerKey = "acb";
      ExportsAdapter cut = getExportsAdapter(true, customerKey);

      Date todayDate = Calendar.getInstance().getTime();
      Calendar tmpCal = Calendar.getInstance();
      tmpCal.set(Calendar.YEAR, tmpCal.get(Calendar.YEAR) + 1);
      String strNextYearDate = TestSupportClientKonstanten.DATE_FORMATTER_YYYY_MM_DD_HH_MM_.format(tmpCal.getTime());
      File theFile = new File("file://fileserver.gee.creditreform.de/gee/bvd/export/delta/" + strNextYearDate + "/");
      PathElement joungestPathElement = new PathElementFileSystem(theFile);
      boolean isJoungestExportDirAfterDate = cut.isJoungestExportDirAfterDate(joungestPathElement, todayDate);
      Assert.assertTrue(isJoungestExportDirAfterDate);

      tmpCal.set(Calendar.YEAR, tmpCal.get(Calendar.YEAR) - 2);
      String strLastYearDate = TestSupportClientKonstanten.DATE_FORMATTER_YYYY_MM_DD_HH_MM_.format(tmpCal.getTime());
      theFile = new File("file://fileserver.gee.creditreform.de/gee/bvd/export/delta/" + strLastYearDate + "/");
      joungestPathElement = new PathElementFileSystem(theFile);
      isJoungestExportDirAfterDate = cut.isJoungestExportDirAfterDate(joungestPathElement, todayDate);
      Assert.assertFalse(isJoungestExportDirAfterDate);

      tmpCal.set(Calendar.YEAR, tmpCal.get(Calendar.YEAR) + 1);
      tmpCal.set(Calendar.MINUTE, tmpCal.get(Calendar.MINUTE) + 1);
      String strNextMinuteDate = TestSupportClientKonstanten.DATE_FORMATTER_YYYY_MM_DD_HH_MM_.format(tmpCal.getTime());
      theFile = new File("file://fileserver.gee.creditreform.de/gee/bvd/export/delta/" + strNextMinuteDate + "/");
      joungestPathElement = new PathElementFileSystem(theFile);
      isJoungestExportDirAfterDate = cut.isJoungestExportDirAfterDate(joungestPathElement, todayDate);
      Assert.assertTrue(isJoungestExportDirAfterDate);
   }

   @Test
   public void testExportsAdapter_retrieveCompletitionInfoFile() throws Exception {
      Calendar tmpCal = Calendar.getInstance();

      // Completition-File aus einem Export, der später als als mein Rentenjahr Jahr erfolgte, suchen: nix da!
      Calendar nextYearCal = Calendar.getInstance();
      nextYearCal.set(Calendar.YEAR, tmpCal.get(Calendar.YEAR) + 30);
      ExportsAdapter exportsAdapter = getExportsAdapter(true, "bvd");
      ByteArrayOutputStream completitionFileBAOS = retrieveCompletitionInfoFile(exportsAdapter, nextYearCal.getTime());
      Assert.assertNull(completitionFileBAOS);

      // Completition-File aus einem Export, der später als letztes Jahr erfolgte, suchen: da!
      Calendar lastYearCal = Calendar.getInstance();
      lastYearCal.set(Calendar.YEAR, tmpCal.get(Calendar.YEAR) - 1);
      completitionFileBAOS = retrieveCompletitionInfoFile(exportsAdapter, lastYearCal.getTime());
      Assert.assertNotNull(completitionFileBAOS);
   }

   @Test
   public void testExportsAdapter_listExportDirs() throws Exception {
      ExportsAdapter exportsAdapter = getExportsAdapter(true, "bvd");
      List<PathElement> pathElements = listDirPathElements(exportsAdapter);
      Assert.assertFalse(pathElements.isEmpty());
      PathElement pathElement0 = pathElements.get(0);
      Assert.assertTrue(pathElement0 instanceof PathElementFileSystem);
      String symbolicPath = pathElement0.getSymbolicPath();
      Assert.assertNotNull(symbolicPath);
      String exportsPath = new File(setupUtil.getEnvironmentConfig().getTestResourcesRoot(), TestSupportClientKonstanten.EXPORTS).getAbsolutePath();
      Assert.assertTrue(symbolicPath.contains(exportsPath));
   }

*/
   @Test
   public void testExportsAdapter_listExportZips() throws Exception {
/*
      ExportsAdapter exportsAdapter = getExportsAdapter(true, "bvd");
      List<PathElement> zipPathElements = listZipPathElements(exportsAdapter);
      Assert.assertFalse(zipPathElements.isEmpty());
      PathElement zipPathElement0 = zipPathElements.get(0);
      String symbolicPath = zipPathElement0.getSymbolicPath();
      Assert.assertNotNull(symbolicPath);
      Assert.assertTrue(symbolicPath.endsWith(".zip"));
*/
   }

}
