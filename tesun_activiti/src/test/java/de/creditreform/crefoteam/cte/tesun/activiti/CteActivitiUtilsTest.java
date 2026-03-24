package de.creditreform.crefoteam.cte.tesun.activiti;

import de.creditreform.crefoteam.activiti.CteActivitiDeployment;
import de.creditreform.crefoteam.activiti.CteActivitiService;
import de.creditreform.crefoteam.activiti.CteActivitiServiceRestImpl;
import de.creditreform.crefoteam.activiti.CteActivitiUtils;
import de.creditreform.crefoteam.activiti.RestInvokerActiviti;
import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class CteActivitiUtilsTest extends TestCase {
    public static final String JUNIT_DEPLOYMENT_NAME = "JUNITTestProcess.bpmn";

    protected CteActivitiUtils cteActivitiUtils;
    CteActivitiService cteActivitiServiceREST;
    @Before
    public void setUp() {
        RestInvokerConfig restInvokerConfig = new RestInvokerConfig("http://rhsctew003.ecofis.de:9999", "CAVDARK-ENE", "cavdark");
        cteActivitiServiceREST = new CteActivitiServiceRestImpl(restInvokerConfig);
        cteActivitiUtils = new CteActivitiUtils(cteActivitiServiceREST);
    }

    @Test
    public void testUploadActivitiProcesses() throws Exception {
        File bpmnFile = new File(getClass().getResource("/" + JUNIT_DEPLOYMENT_NAME).toURI());
        String bpmnFileName = bpmnFile.getAbsolutePath();
        String envName = "ENE";
        boolean askIfExists = true;
        String uploadedActivitiProcessesName = cteActivitiUtils.uploadActivitiProcesses(bpmnFileName, envName, askIfExists);

        CteActivitiDeployment deploymentForName = cteActivitiServiceREST.getDeploymentForName(uploadedActivitiProcessesName);
        Assert.assertNotNull(deploymentForName);

        cteActivitiServiceREST.deleteDeploymentForName(uploadedActivitiProcessesName);
        deploymentForName = cteActivitiServiceREST.getDeploymentForName(uploadedActivitiProcessesName);
        Assert.assertNull(deploymentForName);
    }

    public void testUploadActivitiProcessesFromClassPath() throws Exception {
        String envName = "ENE";
        List<String> uploadedBpmnsList = cteActivitiUtils.uploadActivitiProcessesFromClassPath(envName);
        Assert.assertFalse(uploadedBpmnsList.isEmpty());

        uploadedBpmnsList.forEach(uploadedBpmn -> {
            try {
                cteActivitiServiceREST.deleteDeploymentForName(uploadedBpmn);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}