package de.creditreform.crefoteam.cte.tesun.gui.jvm;

import com.google.common.base.Predicate;
import de.creditreform.crefoteam.cte.tesun.gui.base.model.AbstractMutableTreeNode;
import de.creditreform.crefoteam.jvmclient.domain.JvmInstallation;

import java.util.Map;

public class JvmSpecialJobsTreeNode extends AbstractMutableTreeNode {
    private final Map<String, JvmInstallation> jvmInstallationMap;
    private final Predicate<String> groupPredicate;

    public JvmSpecialJobsTreeNode(String groupName, Map<String, JvmInstallation> jvmInstallationMap, Predicate<String> groupPredicate) {
        super(groupName);
        this.jvmInstallationMap = jvmInstallationMap;
        this.groupPredicate = groupPredicate;
    }

    public Map<String, JvmInstallation> getJvmInstallationMap() {
        return jvmInstallationMap;
    }

    public Predicate<String> getGroupPredicate() {
        return groupPredicate;
    }

    @Override
    public String toString() {
        if (getUserObject() == null) {
            return "...";
        }
        return super.toString();
    }

    @Override
    public int compareTo(Object o) {
        return o.toString().compareTo(this.toString());
    }

    @Override
    public void setActivated(boolean activated) {
    }

    @Override
    public boolean isActivated() {
        return true;
    }
}
