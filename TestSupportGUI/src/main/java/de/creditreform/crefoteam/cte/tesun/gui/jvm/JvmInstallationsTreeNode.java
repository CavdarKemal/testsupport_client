package de.creditreform.crefoteam.cte.tesun.gui.jvm;

import de.creditreform.crefoteam.cte.tesun.gui.base.model.AbstractMutableTreeNode;
import de.creditreform.crefoteam.jvmclient.domain.JvmInstallation;

import java.util.Map;

public class JvmInstallationsTreeNode extends AbstractMutableTreeNode {
    public JvmInstallationsTreeNode(Map<String, JvmInstallation> jvmInstallationMap) {
        super(jvmInstallationMap);
    }

    @Override
    public void setActivated(boolean activated) {

    }

    @Override
    public boolean isActivated() {
        return false;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
