package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class UserTaskBicJobsTest extends UserTaskTestBase {
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testUserTaskBicJobs() throws Exception {
        if(System.getenv("UserTaskBicJobsTest") != null) {
            boolean uploadEmptyPayload = true;
            UserTaskBicJobs cut = new UserTaskBicJobs(environmentConfig, this);
            Map<String, Object> taskParamsMap = new HashMap<>();
            taskParamsMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_UPLOAD_EMPTY_PAYLOAD, uploadEmptyPayload);
            Map<String, Object> resultMap = cut.runTask(taskParamsMap);
            Assert.assertNotNull(resultMap);
        }
        else {
            System.out.println("Test übersprungn, da kein System-Property ' gesetzt ist!");
        }
    }
}
