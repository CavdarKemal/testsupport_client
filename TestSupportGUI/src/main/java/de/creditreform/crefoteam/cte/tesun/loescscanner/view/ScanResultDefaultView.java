package de.creditreform.crefoteam.cte.tesun.loescscanner.view;

import de.creditreform.crefoteam.cte.tesun.loescscanner.design.ScanResultDefaultPanel;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;

import javax.swing.*;

public class ScanResultDefaultView extends ScanResultDefaultPanel {
    private final EnvironmentConfig environmentConfig;
    private final JTree treeCustomers;

    public ScanResultDefaultView(EnvironmentConfig environmentConfig, JTree treeCustomers) {
        this.environmentConfig = environmentConfig;
        this.treeCustomers = treeCustomers;
    }


}
