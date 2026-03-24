package de.creditreform.crefoteam.cte.tesun.nachlieferung;

import de.creditreform.crefoteam.cte.tesun.rest.TesunRestServiceIntegrationTestBase;
import de.creditreform.crefoteam.cte.tesun.util.TestFallFileUtil;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class NachlieferungTest extends TesunRestServiceIntegrationTestBase {

   private static final String umgebung = "ENE";

   public NachlieferungTest() {
      super(umgebung);
   }

   @Test
   public void test_erneuteLieferungBeantragenForCrefosList() throws Exception {
      int NUM_MAX_CREFOS = 10;
      String selectedProject = "CEF";
      URL resourceURL = getClass().getResource("/Missing_LU-1.txt");
      List<Long> crefosList = TestFallFileUtil.readCrefosFromResourceFile(new File(resourceURL.getFile()));
      System.out.println("Erneute Nachlieferung wird beantragt: Umbebung " + umgebung + ", Kunde : " + selectedProject + ", Anzahl Crefos: " + crefosList.size());
      Collection<List<Long>> subList = splitCrefosList(crefosList, NUM_MAX_CREFOS);
      subList.stream().forEach(subCrefosList -> {
         System.out.println("\tSub-List: " + subCrefosList);
         tesunRestServiceJvmImpCycle.erneuteLieferungBeantragen(subCrefosList, selectedProject);
      });
   }

   protected Collection<List<Long>> splitCrefosList(List<Long> crefosList, int chunkSize) {
      final AtomicInteger counter = new AtomicInteger();
      final Collection<List<Long>> result = crefosList.stream()
         .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / chunkSize))
         .values();
      return result;
   }
}
