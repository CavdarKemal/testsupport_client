package de.creditreform.crefoteam.cte.tesun.gui.jvm;

import com.google.common.base.Predicate;
import de.creditreform.crefoteam.jvmclient.domain.JvmInstallation;

import javax.swing.tree.DefaultTreeModel;
import java.util.Map;

public class JvmInstallationsTreeModel extends DefaultTreeModel {
    private final Map<String, JvmInstallation> jvmInstallationMap;

    public JvmInstallationsTreeModel(Map<String, JvmInstallation> jvmInstallationMap) {
        super(new JvmInstallationsTreeNode(jvmInstallationMap));
        this.jvmInstallationMap = jvmInstallationMap;
        initTree();
    }

    private void initTree() {
        JvmInstallationsTreeNode rootNode = (JvmInstallationsTreeNode) super.getRoot();
        for (Map.Entry<String, JvmInstallation> jvmInstallationEntry : jvmInstallationMap.entrySet()) {
            JvmInstallationTreeNode jvmInstallationTreeNode = new JvmInstallationTreeNode(jvmInstallationEntry.getValue());
//         jvmInstallationTreeNode.add(new JvmInstallationTreeNode(null)); // temporäres Node-Element, wird bei Lazy-Loading entfernt!
            rootNode.insert(jvmInstallationTreeNode, rootNode.getChildCount());
        }

        // spezielle Gruppen-Nodes erzeugen...
        JvmSpecialJobsTreeNode fullExportsTreeNode = new JvmSpecialJobsTreeNode("Voll-Expoprts", jvmInstallationMap, new Predicate<String>() {
            @Override
            public boolean apply(String jobName) {
                return jobName.endsWith("fullExport");
            }
        });
        rootNode.insert(fullExportsTreeNode, rootNode.getChildCount());

        // spezielle Gruppen-Nodes erzeugen...
        JvmSpecialJobsTreeNode deltaExportsTreeNode = new JvmSpecialJobsTreeNode("Delta-Expoprts", jvmInstallationMap, new Predicate<String>() {
            @Override
            public boolean apply(String jobName) {
                return jobName.endsWith("deltaExport");
            }
        });
        rootNode.insert(deltaExportsTreeNode, rootNode.getChildCount());

        JvmSpecialJobsTreeNode deltaUploadsTreeNode = new JvmSpecialJobsTreeNode("Delta-Uoploads", jvmInstallationMap, new Predicate<String>() {
            @Override
            public boolean apply(String jobName) {
                return jobName.endsWith("deltaUpload");
            }
        });
        rootNode.insert(deltaUploadsTreeNode, rootNode.getChildCount());

        JvmSpecialJobsTreeNode healthChecksSimpleTreeNode = new JvmSpecialJobsTreeNode("Health-Checks-Simple", jvmInstallationMap, new Predicate<String>() {
            @Override
            public boolean apply(String jobName) {
                return jobName.endsWith("healthChecksSimple");
            }
        });
        rootNode.insert(healthChecksSimpleTreeNode, rootNode.getChildCount());
    }

}
