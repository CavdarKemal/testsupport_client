package de.creditreform.crefoteam.cte.tesun.loescscanner.view;

import de.creditreform.crefoteam.cte.tesun.gui.utils.GUIStaticUtils;
import de.creditreform.crefoteam.cte.tesun.gui.view.ClosableTabbedPane;
import de.creditreform.crefoteam.cte.tesun.loescscanner.design.ScanResultsPanel;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;

import java.io.File;
import java.util.Map;

public class ScanResultsView extends ScanResultsPanel {
    private final float DIVIDER_LOCATION = 0.2f;

    private final ClosableTabbedPane tabbedPaneScanResults = getTabbedPaneScanResults();
    private ScanResultsTabView ScanResultsTabViewMain;
    private EnvironmentConfig environmentConfig;

    public ScanResultsView() {
        tabbedPaneScanResults.setTitleAt(0, "Test Results");
        tabbedPaneScanResults.removeTabAt(0);
    }

    private ScanResultsTabView createScanResultsTabView(String tabName) {
        ScanResultsTabView ScanResultsTabView = new ScanResultsTabView(this, environmentConfig);
        ScanResultsTabView.getSplitPanelTreeView().setDividerLocation(DIVIDER_LOCATION);
        tabbedPaneScanResults.addTab(tabName, ScanResultsTabView);
        tabbedPaneScanResults.setSelectedComponent(ScanResultsTabView);
        return ScanResultsTabView;
    }

    public void doLoadScanResultsFromFile(String lastLoadPath) {
        try {
            String newChoosenFileName = GUIStaticUtils.chooseFileName(this, lastLoadPath, "*.zip", true);
            if ((newChoosenFileName == null)) {
                return;
            }
            File ScanResultsFile = new File(newChoosenFileName);
            ScanResultsTabView ScanResultsTabView = createScanResultsTabView(ScanResultsFile.getName());
            ScanResultsTabView.loadScanResultsFromZipFile(ScanResultsFile);
        } catch (Exception ex) {
            GUIStaticUtils.showExceptionMessage(this, "Fehler beim Laden!", ex);
        }
    }

    public void refresScanResultsForMap(EnvironmentConfig environmentConfig, Map<String, TestCustomer> testCustomerMap) {
        try {
            this.environmentConfig = environmentConfig;
            if (ScanResultsTabViewMain == null) {
                ScanResultsTabViewMain = createScanResultsTabView("Process-Test");
            }
            ScanResultsTabViewMain.getSplitPanelTreeView().setDividerLocation(DIVIDER_LOCATION);
            ScanResultsTabViewMain.initTreeForMap(testCustomerMap);
        } catch (Exception ex) {
            GUIStaticUtils.showExceptionMessage(this, "Fehler beim Aktualisieren!", ex);
        }
    }


}
