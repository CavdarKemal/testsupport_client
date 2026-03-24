package de.creditreform.crefoteam.cte.tesun.xmlsearch.handler;

import de.creditreform.crefoteam.cte.tesun.xmlsearch.domain.*;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.schnittstellen.IMatchInfoListener;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.zip.ZipEntry;

import static org.easymock.EasyMock.*;

/**
 * Test-Klasse für {@link ThreadedSearchHandler}
 */
public class ThreadedSearchHandlerTest {

   // Die synthetischen ZipEntries erhalten einen Namen mit diesem Prefix und einer fortlaufenden Nummer
   protected static final String prefixOfEntryNames = "ZipEntry4Test-";

   private static class ThreadedSearchHandler4Test
   extends ThreadedSearchHandler {

      public ThreadedSearchHandler4Test(SearchSpecification searchSpecification,
                                        XmlMatcherWrapperFactory xmlMatcherWrapperFactory,
                                        XmlStreamListenerGroup streamListenerGroup,
                                        IMatchInfoListener matchInfoListener) {
         super(searchSpecification.getRuntimeSearchSpec(), xmlMatcherWrapperFactory, streamListenerGroup, matchInfoListener);
      }

      @Override
      protected ThreadedSearchAbstract getProducer(BlockingQueue<Runnable> searchTaskQueue, int zipFileNumber, ZipSearcResult.ZipFileInfo zipFileInfo) {
         return new ThreadedSearchProducer4Test(searchTaskQueue, getSearcherService(),
                                                zipFileInfo,
                                                getThreadLocals(),
                                                getMatchInfoListener(), getStreamListenerGroup());
      }

   }

   protected static class ZipStreamWrapper4Test
   implements ThreadedSearchProducer.ZipStreamIF {
      private final int maxEntries;
      private int currentEntry;
      private final ByteArrayInputStream inputStream;

      public ZipStreamWrapper4Test(int maxEntries) {
         this.maxEntries = maxEntries;
         this.inputStream = new ByteArrayInputStream(new byte[0]);
      }

      @Override
      public ZipEntry getNextEntry() throws IOException {
         if (currentEntry<maxEntries) {
            currentEntry++;
            return new ZipEntry(prefixOfEntryNames +currentEntry);
         }
         return null;
      }

      @Override
      public InputStream getInputStream() {
         return inputStream;
      }

      @Override
      public void close() throws IOException {
         currentEntry=0;
      }
   }

   protected static class ThreadedSearchProducer4Test
   extends ThreadedSearchProducer {
      public ThreadedSearchProducer4Test(BlockingQueue<Runnable> taskQueue, ExecutorService searchExecutorService,
                                         ZipSearcResult.ZipFileInfo zipFileInfo,
                                         XmlMatcherThreadLocals xmlMatcherThreadLocals,
                                         IMatchInfoListener matchListener, XmlStreamListenerGroup listenerGroup) {
         super(taskQueue, searchExecutorService, zipFileInfo, xmlMatcherThreadLocals, matchListener, listenerGroup);
      }

      @Override
      protected Callable<SingleEntrySearchResult> createSearchTask(String entryName, BufferedContentInputStream inputStream) {
         return new SearchCallable4Test(entryName);
      }

      @Override
      protected ZipStreamIF getZipStreamWrapperForZipFile(File zipFile) throws FileNotFoundException {
         return new ZipStreamWrapper4Test(100);
      }
   }

   protected static class SearchCallable4Test
   implements Callable<SingleEntrySearchResult> {
      private final String entryName;
      private final boolean doWait;
      private final boolean shalSucceed;

      public SearchCallable4Test(String entryName) {
         this.entryName = entryName;
         final int entryNumber = Integer.valueOf( entryName.substring(prefixOfEntryNames.length()) );
         doWait = (entryNumber%17 == 0);
         shalSucceed = (entryNumber%7 == 0);
      }

      @Override
      public SingleEntrySearchResult call() throws Exception {
         ZipSearcResult.ZipEntryInfo zipEntryInfo = new ZipSearcResult.ZipEntryInfo(null, null, entryName);
         if (doWait) {
            Thread.sleep(200);
         }
         return new SingleEntrySearchResult(shalSucceed, zipEntryInfo, null);
      }

   }

   private List<Object> listOfMocks;

   @Before
   public void setUp() {
      listOfMocks = new ArrayList<>();
   }

   private SearchSpecification createSearchSpecificationProcessor() {
      SearchSpecification mock = createMock(SearchSpecification.class);
      expect(mock.getRuntimeSearchSpec()).andReturn(null).once();
      listOfMocks.add(mock);
      return mock;
   }

   private XmlMatcherWrapperFactory createXmlMatcherWrapperFactory() {
      XmlMatcherWrapperFactory mock = createMock(XmlMatcherWrapperFactory.class);
      listOfMocks.add(mock);
      return mock;
   }

   private XmlStreamListenerGroup createXmlStreamListenerGroup() {
//      ProgressListenerIF progressListener = createMock(ProgressListenerIF.class);
//      expect(progressListener.isCanceled()).andReturn(false).anyTimes();
//      progressListener.updateData( anyObject() );
//      expectLastCall().anyTimes();
//      progressListener.updateProgress();
//      expectLastCall().anyTimes();
//      progressListener.updateTaskState(anyObject());
//      expectLastCall().anyTimes();
//      listOfMocks.add(progressListener);

      ProgressListenerIF progressListener = new ProgressListenerNop();
      XmlStreamListenerGroup listenerGroup = new XmlStreamListenerGroup(null, progressListener);

      return listenerGroup;
   }

   private IMatchInfoListener createMatchInfoListener() {
      IMatchInfoListener mock = createMock(IMatchInfoListener.class);
      listOfMocks.add(mock);
      return mock;
   }

   private ThreadedSearchHandler4Test createHandler4Test() {
      SearchSpecification searchSpecification = createSearchSpecificationProcessor();
      XmlMatcherWrapperFactory xmlMatcherWrapperFactory = createXmlMatcherWrapperFactory();
      XmlStreamListenerGroup xmlStreamListenerGroup = createXmlStreamListenerGroup();
      IMatchInfoListener matchInfoListener = createMatchInfoListener();
      EasyMock.replay(listOfMocks.toArray());
      ThreadedSearchHandler4Test handler4Test = new ThreadedSearchHandler4Test(searchSpecification,
                                                                               xmlMatcherWrapperFactory,
                                                                               xmlStreamListenerGroup,
                                                                               matchInfoListener);
      return handler4Test;
   }

   @Test
   public void testCreateInstance() {
      ThreadedSearchHandler handler = createHandler4Test();
      Assert.assertNotNull(handler);
   }

   @Test
   public void testProcessNonExistantZip() {
      ThreadedSearchHandler handler = createHandler4Test();
      ZipSearcResult.ZipFileInfo zipFileInfo1 = new ZipSearcResult.ZipFileInfo(null, "das-gibt-es-nicht.zip", new File("."));
      ZipSearcResult.ZipFileInfo zipFileInfo2 = new ZipSearcResult.ZipFileInfo(null, "das-gibt-es-auch-nicht.zip", new File("."));
      handler.submitZipFile(zipFileInfo1);
      handler.submitZipFile(zipFileInfo2);
      handler.waitForCompletion();
      Assert.assertEquals("Anzahl der Einträge in den simulierten Zip-Dateien falsch", 200, handler.getNumEntriesProcessed()); // 2*100
      Assert.assertEquals("Anzahl der Treffer in den simulierten Zip-Dateien falsch", 28, handler.getNumMatches()); // 2*100/7
      Assert.assertTrue("Der Producer sollte mehrere Pausen eingelegt haben", handler.getSleepingRounds()>0);
      Assert.assertEquals(ThreadedSearchPerZipResult.StateAtExit.SUCCESS, handler.getAggregatedState());
   }

}
