package de.creditreform.crefoteam.cte.tesun.gui.jvm;

import de.creditreform.crefoteam.cte.tesun.gui.base.model.AbstractMutableTreeNode;
import de.creditreform.crefoteam.jvmclient.domain.JvmInstallation;

public class JvmInstallationTreeNode extends AbstractMutableTreeNode {
    public JvmInstallationTreeNode(JvmInstallation jvmInstallation) {
        super(jvmInstallation);
    }

    @Override
    public String toString() {
        if (getUserObject() == null) {
            return "Job-Liste wird geladen...";
        }
        JvmInstallation jvmInstallation = (JvmInstallation) getUserObject();
        String jvmName = jvmInstallation.getJvmName();
        return jvmName;
    }

    @Override
    public int compareTo(Object o) {
        JvmInstallation theObj = (JvmInstallation) o;
        JvmInstallation myObj = (JvmInstallation) getUserObject();
        return theObj.getJvmName().compareTo(myObj.getJvmName());
    }

    @Override
    public void setActivated(boolean activated) {
        JvmInstallation myObj = (JvmInstallation) getUserObject();
        myObj.setActivated(activated);
    }

    @Override
    public boolean isActivated() {
        JvmInstallation myObj = (JvmInstallation) getUserObject();
        return myObj.isActivated();
    }
}
