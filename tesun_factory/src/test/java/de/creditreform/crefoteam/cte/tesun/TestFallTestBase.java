package de.creditreform.crefoteam.cte.tesun;

import de.creditreform.crefoteam.cte.tesun.TesunClientJob.JOB_RESULT;
import de.creditreform.crefoteam.cte.tesun.testutil.TesunTestSetupUtil;
import de.creditreform.crefoteam.cte.tesun.util.ClientJobStarter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

public abstract class TestFallTestBase {
   protected final TesunTestSetupUtil setupUtil = new TesunTestSetupUtil();
   protected List<String> notifyList = new ArrayList<>();

   @Before
   public void setUp() {
      setupUtil.setUp().installAppender();
      notifyList.clear();
   }

   @After
   public void teardown() {
      setupUtil.teardown();
   }

   protected TesunClientJobListener tesunClientJobListener = new TesunClientJobListener() {
      @Override
      public void notifyClientJob(Level level, Object notifyObject) {
         notifyList.add(notifyObject.toString());
         setupUtil.logger.info(notifyObject.toString());
      }

      @Override
      public Object askClientJob(ASK_FOR askFor, Object userObject) {
         return null;
      }
   };

   protected void deleteFilesInDir(File ab30XmlsDir) {
      Collection<File> fileCollection = FileUtils.listFiles(ab30XmlsDir, null, false);
      fileCollection.forEach(file -> file.delete());
   }

   public List<String> getNotifyList() {
      return notifyList;
   }

   protected void startJobTest(ClientJobStarter jobStarter) {
      try {
         JOB_RESULT jobResult = jobStarter.startJob(setupUtil.getEnvironmentConfig());
         if (jobResult.equals(JOB_RESULT.ERROR)) {
            Assert.fail(((Exception)jobResult.getUserObject()).getMessage());
         }
      } finally {
      }
   }

}
