package de.creditreform.crefoteam.cte.tesun.exports_collector;

import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.jobexecution.TesunJobexecutionInfo;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TimelineLogger;
import org.apache.log4j.Level;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class ExportsWaiter {
    final EnvironmentConfig environmentConfig;
    final TesunClientJobListener tesunClientJobListener;

    public ExportsWaiter(EnvironmentConfig environmentConfig, TesunClientJobListener tesunClientJobListener) {
        this.environmentConfig = environmentConfig;
        this.tesunClientJobListener = tesunClientJobListener;
    }

    public void waitForExports(Map<String, TestCustomer> customerTestInfoMap) throws Exception {
        long timeOutMillis = environmentConfig.getMillisForExportsTimeOut();
        long sleepTimeMillis = environmentConfig.getMillisForJobStatusQuerySleepTime();
        RestInvokerConfig restServiceConfigTesun = environmentConfig.getRestServiceConfigsForMasterkonsole().get(0);
        ExecutorService executor = Executors.newFixedThreadPool(5);
        Map<TestCustomer, FutureTask<TesunJobexecutionInfo>> jobExecInfoMap = new HashMap<>();
        List<String> exportJobNameList = new ArrayList<>();
        for (Map.Entry<String, TestCustomer> entry : customerTestInfoMap.entrySet()) {
            final TestCustomer testCustomer = entry.getValue();
            if (testCustomer.isActivated()) {
                // wurde für die JVM schon ein Future erstellt?
                if (exportJobNameList.contains(testCustomer.getExportJobName())) {
                    continue;
                }
                exportJobNameList.add(testCustomer.getExportJobName());
                String strInfo = String.format("\n\tWarte auf den Exports %s...", testCustomer.getProcessIdentifier());
                tesunClientJobListener.notifyClientJob(Level.INFO, strInfo);
                JobExecutionInfoCallable callable = new JobExecutionInfoCallable<>(restServiceConfigTesun, testCustomer, tesunClientJobListener);
                callable.setTimeOutMillis(timeOutMillis);
                callable.setSleepTimeMillis(sleepTimeMillis);
                FutureTask<TesunJobexecutionInfo> futureTask = new FutureTask<>(callable);
                jobExecInfoMap.put(testCustomer, futureTask);
                executor.execute(futureTask);
                Thread.sleep(100);
            }
        }
        try {
            for (Map.Entry<TestCustomer, FutureTask<TesunJobexecutionInfo>> entry : jobExecInfoMap.entrySet()) {
                FutureTask<TesunJobexecutionInfo> futureTask = entry.getValue();
                TesunJobexecutionInfo tesunJobexecutionInfo = futureTask.get();
                TimelineLogger.end(tesunJobexecutionInfo.getInfoKey(), "");
            }
        } finally {
            executor.shutdown();
        }
    }

}
