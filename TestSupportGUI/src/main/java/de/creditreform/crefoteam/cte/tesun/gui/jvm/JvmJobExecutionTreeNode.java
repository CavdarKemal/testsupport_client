package de.creditreform.crefoteam.cte.tesun.gui.jvm;

import de.creditreform.crefoteam.cte.tesun.gui.base.model.AbstractMutableTreeNode;
import de.creditreform.crefoteam.jvmclient.domain.JobStartResponse;
import de.creditreform.crefoteam.jvmclient.domain.JvmJobExecutionInfo;

public class JvmJobExecutionTreeNode extends AbstractMutableTreeNode {
    public JvmJobExecutionTreeNode(JvmJobExecutionInfo jvmJobExecutionInfo) {
        super(jvmJobExecutionInfo);
    }

    @Override
    public String toString() {
        if (getUserObject() == null) {
            return "...";
        }
        JvmJobExecutionInfo jvmJobExecutionInfo = (JvmJobExecutionInfo) getUserObject();
        return jvmJobExecutionInfo.toString();
    }

    @Override
    public int compareTo(Object o) {
        JvmJobExecutionInfo theObj = (JvmJobExecutionInfo) o;
        JvmJobExecutionInfo myObj = (JvmJobExecutionInfo) getUserObject();
        return theObj.getJobId().compareTo(myObj.getId());
    }

    @Override
    public void setActivated(boolean activated) {
        JvmJobExecutionInfo jvmJobExecutionInfo = (JvmJobExecutionInfo) getUserObject();
        // TODO
    }

    @Override
    public boolean isActivated() {
        JobStartResponse jobExecutionInfo = (JobStartResponse) getUserObject();
        // TODO
        return true;
    }

}
