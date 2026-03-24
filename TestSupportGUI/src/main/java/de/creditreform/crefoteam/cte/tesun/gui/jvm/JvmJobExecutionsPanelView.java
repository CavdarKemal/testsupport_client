package de.creditreform.crefoteam.cte.tesun.gui.jvm;

import de.creditreform.crefoteam.cte.tesun.jvm.JvmJobExecutionsPanel;
import de.creditreform.crefoteam.jvmclient.domain.JvmJobExecutionInfo;
import de.creditreform.crefoteam.jvmclient.domain.JvmJobInfo;

public class JvmJobExecutionsPanelView extends JvmJobExecutionsPanel {
    private final JvmJobExecutionTreeNode jvmJobExecutionTreeNode;

    public JvmJobExecutionsPanelView(JvmJobExecutionTreeNode jvmJobExecutionTreeNode) {
        this.jvmJobExecutionTreeNode = jvmJobExecutionTreeNode;
        initControls();
    }

    private void initControls() {
        JvmJobExecutionInfo JvmJobExecutionInfo = (JvmJobExecutionInfo) jvmJobExecutionTreeNode.getUserObject();
        getTextFieldId().setText(JvmJobExecutionInfo.getId());
        getTextFieldJobId().setText(JvmJobExecutionInfo.getJobId());
        getTextFieldJobName().setText(JvmJobExecutionInfo.getJobName());
        getTextFieldStatus().setText(JvmJobExecutionInfo.getStatus());
        getTextFieldExitCode().setText(JvmJobExecutionInfo.getExitCode());
        getTextFieldRunning().setText(JvmJobExecutionInfo.getRunning());
        getTextFieldStartDate().setText(JvmJobInfo.formatDateTime(JvmJobExecutionInfo.getStartDate()));
        getTextFieldEndDate().setText(JvmJobInfo.formatDateTime(JvmJobExecutionInfo.getEndDate()));
    }

}
