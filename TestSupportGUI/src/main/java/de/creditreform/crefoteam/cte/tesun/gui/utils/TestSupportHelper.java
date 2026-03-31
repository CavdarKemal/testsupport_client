package de.creditreform.crefoteam.cte.tesun.gui.utils;

import de.creditreform.crefoteam.activiti.CteActivitiProcess;
import de.creditreform.crefoteam.activiti.CteActivitiService;
import de.creditreform.crefoteam.activiti.CteActivitiServiceRestImpl;
import de.creditreform.crefoteam.activiti.CteActivitiTask;
import de.creditreform.crefoteam.cte.rest.RestInvoker;
import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import de.creditreform.crefoteam.cte.rest.RestInvokerResponse;
import de.creditreform.crefoteam.cte.rest.apache4.Apache4RestInvokerFactory;
import de.creditreform.crefoteam.cte.rest.interfaces.RestInvokerFactory;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.pendingjobs.TesunPendingJob;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.pendingjobs.TesunPendingJobs;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.JobInfo;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.apache.commons.io.IOUtils;
import org.apache.http.impl.execchain.RequestAbortedException;
import org.apache.log4j.Level;

public class TestSupportHelper {

    private final CteActivitiService cteActivitiService;
    private final TesunRestService tesunRestServiceWLS;
    private final TesunRestService tesunRestServiceJvmImportC;
    private final TesunClientJobListener tesunClientJobListener;
    private final EnvironmentConfig environmentConfig;

    public TestSupportHelper(EnvironmentConfig environmentConfig, RestInvokerConfig activitiRestInvokerConfig, RestInvokerConfig masterConsoleRestInvokerConfig, RestInvokerConfig impCyleRestInvokerConfig, TesunClientJobListener tesunClientJobListener) throws Exception {
        this.environmentConfig = environmentConfig;
        this.tesunClientJobListener = tesunClientJobListener;
        cteActivitiService = new CteActivitiServiceRestImpl(activitiRestInvokerConfig);
        tesunRestServiceWLS = new TesunRestService(masterConsoleRestInvokerConfig, tesunClientJobListener);
        tesunRestServiceJvmImportC = new TesunRestService(impCyleRestInvokerConfig, tesunClientJobListener);
    }

    public void checkStartCoinditions(Map<String, TestCustomer> activeTestCustomersMap, boolean confirmDlg) throws Exception {
        tesunClientJobListener.notifyClientJob(Level.INFO, "\nPrüfe die Prozess-Start-Bedinbungen...");
        String errString;
        errString = tesunRestServiceJvmImportC.pruefeKundenInstallation(activeTestCustomersMap);
        if (!errString.isEmpty()) {
            throw new RuntimeException(errString);
        }
        errString = checkRunningJobs(activeTestCustomersMap);
        if (!errString.isEmpty()) {
            int confirmOpt =  (int)tesunClientJobListener.askClientJob(TesunClientJobListener.ASK_FOR.ASK_OBJECT_RETRY, errString);
            if (confirmOpt == 1) {
                throw new RuntimeException(errString);
            }
        }
        errString = checkJvms(activeTestCustomersMap);
        if (!errString.isEmpty()) {
            throw new RuntimeException(errString);
        }
    }

    public CteActivitiTask killOrContinueRunningActivitiProcess(String prozessKey, String prozessDefName, boolean confirmDlg) throws Exception {
        Map<String, Object> processVarsMap = new HashMap<>();
        processVarsMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_MEIN_KEY, prozessKey);
        // Zweistufig: erst Process-Instance-ID ermitteln (GET ohne Variablen-JOIN, client-seitig gefiltert),
        // dann Tasks per processInstanceId abfragen
        List<CteActivitiProcess> runningProcesses = cteActivitiService.queryProcessInstances(prozessDefName, processVarsMap);
        if (runningProcesses.isEmpty()) {
            return null;
        }
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_MEIN_KEY, prozessKey);
        paramsMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_ACTIVITI_PROCESS_NAME, prozessDefName);
        Integer processInstanceId = runningProcesses.get(0).getId();
        Map<String, Object> taskQueryMap = new HashMap<>();
        taskQueryMap.put("processInstanceId", processInstanceId.toString());
        List<CteActivitiTask> cteActivitiTasksList = cteActivitiService.listTasks(taskQueryMap);
        if (!cteActivitiTasksList.isEmpty()) {
            if (confirmDlg) {
                String strInfo = String.format("Der Prozess wurde zuvor gestartet und steht beim User-Task '%s'!\nSoll der Prozess fortgesetzt oder beendet und neu gestartet werden?", cteActivitiTasksList.get(0).getTaskDefinitionKey());
                int answer = (int)tesunClientJobListener.askClientJob(TesunClientJobListener.ASK_FOR.ASK_OBJECT_CONTINUE, strInfo);
                if (answer == JOptionPane.YES_OPTION) {
                    return cteActivitiTasksList.get(0);
                }
                else if (answer == JOptionPane.CANCEL_OPTION) {
                    throw new RequestAbortedException("Aborted!");
                }
                killRunningActivitiProcess(paramsMap);
                return null;
            }
        }
        return null;
    }

    private boolean killRunningActivitiProcess(Map<String, Object> paramsMap) throws Exception {
        String processDefinitionKey = (String) paramsMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_ACTIVITI_PROCESS_NAME);
        List<CteActivitiProcess> processInstancesList = cteActivitiService.queryProcessInstances(processDefinitionKey, paramsMap);
        if (!processInstancesList.isEmpty()) {
            for (CteActivitiProcess processInstance : processInstancesList) {
                tesunClientJobListener.notifyClientJob(Level.INFO, "\nLösche Prozess processDefinitionKey:" + processInstance.getId());
                cteActivitiService.deleteProcessInstance(processInstance.getId());
            }
        }
        return true;
    }

    public String checkRunningJobs(Map<String, TestCustomer> activeTestCustomersMap) throws Exception {
        tesunClientJobListener.notifyClientJob(Level.INFO, "\nPrüfe, ob Jobs aktiv sind...");
        StringBuilder errStringBuilder = new StringBuilder();
        StringBuilder jobsStringBuilder = new StringBuilder();
        final RestInvokerConfig restServiceConfigTesun = environmentConfig.getRestServiceConfigsForMasterkonsole().get(0);
        TesunRestService tesunRestService = new TesunRestService(restServiceConfigTesun, tesunClientJobListener);
        TesunPendingJobs tesunPendingJobs = tesunRestService.getTesunPendingJobs();
        List<TesunPendingJob> pendingJobsList = tesunPendingJobs.getJobs();
        if (!pendingJobsList.isEmpty()) {
            errStringBuilder.append("\nDer Test in der Umgebung '");
            errStringBuilder.append(environmentConfig.getCurrentEnvName());
            errStringBuilder.append("' kann nicht gestartet werden, solange noch JVM-Jobs aktiv sind!");
            errStringBuilder.append("\nEs sind derzeit folgende JVM-Jobs aktiv:");
            for (TesunPendingJob tesunPendingJob : pendingJobsList) {
                String theKey = tesunPendingJob.getProzessIdentifier().replace("EXPORT_CTE_TO_", "");
                if (activeTestCustomersMap.containsKey(theKey)) {
                    jobsStringBuilder.append("\n\t");
                    jobsStringBuilder.append(tesunPendingJob.getProzessIdentifier());
                    jobsStringBuilder.append(" mit ");
                    jobsStringBuilder.append(tesunPendingJob.getAnzahlTodoBloecke());
                    jobsStringBuilder.append(" Blöcken\n");
                }
            }
            if (!jobsStringBuilder.toString().isEmpty()) {
                errStringBuilder.append(jobsStringBuilder);
                errStringBuilder.append("\nSoll der Test dennoch gestartet werden?");
            }
        }
        return jobsStringBuilder.toString().isEmpty() ? "" : errStringBuilder.toString();
    }

    public String checkJvms(Map<String, TestCustomer> activeTestCustomersMap) throws Exception {
        tesunClientJobListener.notifyClientJob(Level.INFO, "\nPrüfe, ob alle JVM's erreichbar sind...");
        TesunRestService tesunRestServiceCteBatchGUI = new TesunRestService(environmentConfig.getRestServiceConfigsForBatchGUI().get(0), tesunClientJobListener);
        Map<String, String> jvmInstallationMap = tesunRestServiceCteBatchGUI.getJvmInstallationMap();
        JobInfo jobInfoForImportCycle = environmentConfig.getJobInfoForImportCycle();

        StringBuilder errStringBuilder = new StringBuilder();
        List<String> notReachableJvmsList = new ArrayList<>();
        RestInvokerConfig restServiceConfigTesun = environmentConfig.getRestServiceConfigsForMasterkonsole().get(0);
        RestInvokerFactory restInvokerFactory = new Apache4RestInvokerFactory(restServiceConfigTesun.getServiceUser(), restServiceConfigTesun.getServicePassword(), 10000);
        for (Map.Entry<String, String> entry : jvmInstallationMap.entrySet()) {
            if (activeTestCustomersMap.containsKey(entry.getKey()) || jobInfoForImportCycle.getJvmName().equals(entry.getKey())) {
                try {
                    RestInvoker restInvoker = restInvokerFactory.getRestInvoker(entry.getValue());
                    restInvoker.appendPath("/jvm-info/maven-modules");// http://rhsctew002.ecofis.de:7062/jvm-info/maven-modules
                    RestInvokerResponse restInvokerResponse = restInvoker.invokeGetWithRetry(null, 10, 5000L).expectStatusOK();
                    String responseBody = restInvokerResponse.getResponseBody();
/*
               JAXBContext jaxbContext = JAXBContext.newInstance(MavenModuleList.class);
               MavenModuleList mavenModuleList = (MavenModuleList )jaxbContext.createUnmarshaller().unmarshal(new StringReader(responseBody));
*/
                    String s1 = "<maven-module-list";
                    if (!responseBody.contains(s1)) {
                        throw new RuntimeException("getMavenModulesForJvm() : Response enthält nicht: " + s1);
                    }
                    String s2 = "<java-client-id>" + entry.getKey().toLowerCase().substring(0, 3) + "</java-client-id>";
                    if (!responseBody.contains(s2)) {
                        throw new RuntimeException("getMavenModulesForJvm() : Response enthält nicht: " + s2);
                    }
                    tesunClientJobListener.notifyClientJob(Level.INFO, String.format("\n\tJVM '%s' mit der URL '%s' ist erreichbar.", entry.getKey(), entry.getValue()));
                } catch (Exception ex) {
                    notReachableJvmsList.add(entry.getKey());
                } finally {
                    restInvokerFactory.close();
                }
            }
        }
        if (!notReachableJvmsList.isEmpty()) {
            errStringBuilder.append(String.format("\nFolgende JVM's konnten in der Umgebung '%s' nicht angesprochen werden!\n", environmentConfig.getCurrentEnvName()));
            errStringBuilder.append(notReachableJvmsList);
        }
        return errStringBuilder.toString();
    }

    public Dimension getScaledDimension(JLabel jLabel, BufferedImage lastProcessImage) {
        Dimension imageSize = new Dimension(lastProcessImage.getWidth(), lastProcessImage.getHeight());
        Dimension boundary = new Dimension(jLabel.getWidth(), jLabel.getHeight());
        double widthRatio = boundary.getWidth() / imageSize.getWidth();
        double heightRatio = boundary.getHeight() / imageSize.getHeight();
        double ratio = Math.min(widthRatio, heightRatio);
        Dimension scaledDimension = new Dimension((int) (imageSize.width * ratio), (int) (imageSize.height * ratio));
        return scaledDimension;
    }

    public BufferedImage refreshProcessImage(InputStream inputStream, JLabel jLabel, boolean resizeProcessImage) throws IOException {
        if (inputStream == null) {
            return null;
        }
        InputStream bufferedInputStream = IOUtils.toBufferedInputStream(inputStream);
        byte[] byteArray = IOUtils.toByteArray(bufferedInputStream);
        InputStream temp2 = new ByteArrayInputStream(byteArray);
        BufferedImage lastProcessImage = ImageIO.read(temp2);
        if (resizeProcessImage) {
            Dimension scaledDimension = getScaledDimension(jLabel, lastProcessImage);
            Image resizedImage = lastProcessImage.getScaledInstance((int) scaledDimension.getWidth(), (int) scaledDimension.getHeight(), java.awt.Image.SCALE_DEFAULT);
            jLabel.setIcon(new ImageIcon(resizedImage));
        } else {
            jLabel.setIcon(new ImageIcon(lastProcessImage));
        }
        temp2.close();
        inputStream.close();
        return lastProcessImage;
    }

    public CteActivitiService getActivitiRestService() {
        return cteActivitiService;
    }

    public TesunRestService getTesunRestServiceWLS() {
        return tesunRestServiceWLS;
    }

}
