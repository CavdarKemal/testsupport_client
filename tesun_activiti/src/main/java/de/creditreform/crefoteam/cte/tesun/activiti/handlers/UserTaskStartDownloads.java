package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.downloader.TestFallDownloader;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.cte.tesun.util.TesunUtilites;
import org.apache.log4j.Level;

import java.io.File;
import java.util.List;
import java.util.Map;

public class UserTaskStartDownloads extends AbstractUserTaskRunnable {

    // GET http://http://rhsctem015.ecofis.de:7077/cte-tesun-service/tesun/xmlaccess/6072001503

    public UserTaskStartDownloads(final EnvironmentConfig environmentConfig, TesunClientJobListener tesunClientJobListener) {
        super(environmentConfig, tesunClientJobListener);
    }

    @Override
    public Map<String, Object> runTask(Map<String, Object> taskVariablesMap) throws Exception {
        TestSupportClientKonstanten.TEST_PHASE testPhase = (TestSupportClientKonstanten.TEST_PHASE) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_PHASE);
        notifyUserTask(Level.INFO, buildNotifyStringForClassName(testPhase));
        if (checkDemoMode((Boolean)taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_DEMO_MODE))) {
            return taskVariablesMap;
        }
        Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> selectedCustomersMapMap = (Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>>) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_ACTIVE_CUSTOMERS);
        Map<String, TestCustomer> selectedCustomersMapPhaseX = selectedCustomersMapMap.get(testPhase);
        doStartTesunClientJob(new TestFallDownloader(selectedCustomersMapPhaseX, tesunClientJobListener, testPhase));
        // check, ob Fehler auftrat...
        File srcDir = environmentConfig.getNewTestCasesRoot();
        List<File> errorTxtFiles = TesunUtilites.getErrorFilesFor(srcDir);
        if (errorTxtFiles.size() > 0) {
            Boolean exportsOK = (Boolean) askUserTask(TesunClientJobListener.ASK_FOR.ASK_OBJECT_CHECK_DOWNLOADS, errorTxtFiles);
            if (!exportsOK) {
                throw new RuntimeException("\nProzess abgebrochen!");
            }
        }
        testResultsZipHandler.writeTestResultsToFile(selectedCustomersMapPhaseX);
        return taskVariablesMap;
    }

}
