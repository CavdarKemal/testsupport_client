package de.creditreform.crefoteam.cte.tesun.loescscanner.view;

import de.creditreform.crefoteam.cte.tesun.gui.model.DifferenceTreeNode;
import de.creditreform.crefoteam.cte.tesun.gui.utils.GUIStaticUtils;
import de.creditreform.crefoteam.cte.tesun.gui.view.TestCustomerTreeCellRenderer;
import de.creditreform.crefoteam.cte.tesun.loescscanner.design.ScanResultsTabPanel;
import de.creditreform.crefoteam.cte.tesun.loescscanner.model.ScanResults;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.PropertiesException;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestScenario;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ScanResultsTabView extends ScanResultsTabPanel implements TreeSelectionListener {
    private String lastLoadPath;
    private String lastSavedPath;
    private ScanResultsView ScanResultsView;
    private EnvironmentConfig environmentConfig;
    private Map<String, TestCustomer> testCustomerMap;
    private final Map<String, Component> cardViewsMap = new TreeMap<>();
    Component currentCardView;
    private final JTree treeCustomers;

    public ScanResultsTabView() {
        initListeners();
        getButtonSaveScanResults().setEnabled(false);
        treeCustomers = getTreeCustomers();
//        ScanResultsTreeModel ScanResultsTreeModel = new ScanResultsTreeModel(new HashMap<>());
//        treeCustomers.setModel(ScanResultsTreeModel);
        treeCustomers.setCellRenderer(new TestCustomerTreeCellRenderer());
        treeCustomers.addMouseListener(doDoubClickOnTree());
    }

    public ScanResultsTabView(ScanResultsView ScanResultsView, EnvironmentConfig environmentConfig) {
        this();
        this.ScanResultsView = ScanResultsView;
        this.environmentConfig = environmentConfig;
        try {
            lastLoadPath = environmentConfig.getTestOutputsRoot().getAbsolutePath();
            String fileName = environmentConfig.getCurrentEnvName() + "--CTE-" + environmentConfig.getCteVersion() + "-ScanResults.zip";
            lastSavedPath = new File(environmentConfig.getTestOutputsRoot(), fileName).getAbsolutePath();
//            ScanResultsZipHandler = new ScanResultsZipHandler(environmentConfig);
        } catch (PropertiesException ex) {
            GUIStaticUtils.showExceptionMessage(this, "Fehler beim Initialisieren!", ex);
        }
        initControls();
    }

    private void initControls() {
        try {
            getButtonStartDifTool().setEnabled(false);
            List<File> diffToolsList = environmentConfig.getDiffToolsList();
            diffToolsList.forEach(diffTool -> {
                getComboBoxDiffTools().addItem(diffTool);
            });
        } catch (Exception ex) {
            GUIStaticUtils.showExceptionMessage(this, "Fehler beim Initialisieren!", ex);
        }
    }

    private void initListeners() {
        getButtonLoadScanResults().addActionListener(event -> {
            ScanResultsView.doLoadScanResultsFromFile(lastLoadPath);
        });
        getButtonSaveScanResults().addActionListener(event -> {
            String newChoosenFileName = GUIStaticUtils.chooseFileName(this, lastSavedPath, "*.zip", false);
            if ((newChoosenFileName == null)) {
                return;
            }
            doSaveScanResults(newChoosenFileName);
        });
        getButtonRefreshScanResults().addActionListener(event -> {
            if (testCustomerMap != null) {
                initTreeForMap(testCustomerMap);
            }
        });
        getButtonStartDifTool().addActionListener(e -> doStartDiffTool());
    }

    public void doStartDiffTool() {
        File selectedItem = (File) getComboBoxDiffTools().getSelectedItem();
        if (selectedItem == null) {
            return;
        }
        GUIStaticUtils.setWaitCursor(this, true);
        this.setEnabled(false);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
//                File xmlFileSrc = diffenrenceInfo.getXmlFileSrc();
//                File xmlFileDst = diffenrenceInfo.getXmlFileDst();
//                List<String> execCmdsList = new ArrayList<>();
//                execCmdsList.add(selectedItem.getAbsolutePath());
//                execCmdsList.add(xmlFileSrc.getAbsolutePath());
//                execCmdsList.add(xmlFileDst.getAbsolutePath());
//                ProcessBuilder builder = new ProcessBuilder(execCmdsList);
//                try {
//                    Process theProcess = builder.start();
//                    while (theProcess.isAlive()) {
//                        Thread.sleep(300);
//                    }
//                    theProcess.destroy();
//                    String infoFromErrStream = getInfoFromErrorStream(theProcess);
//                    if (!infoFromErrStream.isEmpty()) {
//                        GUIStaticUtils.showExceptionMessage(ScanResultsTabView.this, "Fehler beim Starten des Diff-Tools!\n\t" + infoFromErrStream, null);
//                    }
//                } catch (Exception ex) {
//                    GUIStaticUtils.showExceptionMessage(ScanResultsTabView.this, "Fehler beim Starten des Diff-Tools!\n\t", ex);
//                }
            }
        });
        thread.start();
        GUIStaticUtils.setWaitCursor(this, false);
        this.setEnabled(true);
    }

    private String getInfoFromErrorStream(Process theProcess) throws IOException {
        StringBuilder strOutput = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(theProcess.getErrorStream()));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            strOutput.append(line);
            strOutput.append("\n");
        }
        return strOutput.toString();
    }

    public void initTreeForMap(Map<String, TestCustomer> testCustomerMap) {
        this.testCustomerMap = testCustomerMap;
        GUIStaticUtils.setWaitCursor(this, true);
        this.setEnabled(false);

        TreeSelectionListener[] treeSelectionListeners = getTreeCustomers().getTreeSelectionListeners();
        if (treeSelectionListeners.length > 0) {
            treeCustomers.removeTreeSelectionListener(treeSelectionListeners[0]);
        }
//        ScanResultsTreeModel ScanResultsTreeModel = new ScanResultsTreeModel(testCustomerMap);
//        treeCustomers.setModel(ScanResultsTreeModel);
        treeCustomers.setSelectionRow(0);
        treeCustomers.setSelectionPath(null);
        treeCustomers.addTreeSelectionListener(this::valueChanged);
        treeCustomers.setSelectionPath(new TreePath(treeCustomers.getModel().getRoot()));
        this.setEnabled(true);
        GUIStaticUtils.setWaitCursor(this, false);
    }

    public void loadScanResultsFromZipFile(File ScanResultsZipFile) {
        GUIStaticUtils.setWaitCursor(this, true);
        this.setEnabled(false);
        try {
//            Map<String, TestCustomer> testCustomerMap = ScanResultsZipHandler.readScanResultsFromZipFile(ScanResultsZipFile);
            initTreeForMap(testCustomerMap);
        } catch (Exception ex) {
            GUIStaticUtils.showExceptionMessage(this, "Fehler beim Laden!", ex);
        }
        GUIStaticUtils.setWaitCursor(this, false);
        this.setEnabled(true);
    }

    public void doSaveScanResults(String zipFileName) {
        GUIStaticUtils.setWaitCursor(this, true);
        this.setEnabled(false);
        try {
//            ScanResultsZipHandler.writeScanResultsToZipFile(testCustomerMap, new File(zipFileName));

            StringBuilder stringBuilder = dumAllCustomers();
            File txtFile = new File(zipFileName.replace("zip", "txt"));
            FileUtils.writeStringToFile(txtFile, stringBuilder.toString());

            lastSavedPath = zipFileName;
            String message = "Die Test-Ergebnisse sind ind der Datei \n\t" + txtFile.getAbsolutePath() + "\nund\n\t" + zipFileName + "\ngespeichert.";
            JOptionPane.showMessageDialog(this, message, "Datei Speichern", JOptionPane.OK_OPTION);
        } catch (Exception ex) {
            GUIStaticUtils.showExceptionMessage(this, "Fehler beim Speichern!", ex);
        }
        GUIStaticUtils.setWaitCursor(this, false);
        this.setEnabled(true);
    }

    public void setText(String newContent) {
        JTextArea textAreaFileSrc = ((ScanResultDefaultView) currentCardView).getTextAreaFileSrc();
        textAreaFileSrc.setText("");
        textAreaFileSrc.setTabSize(2);
        textAreaFileSrc.setCaretPosition(0);
        textAreaFileSrc.setText(newContent);
        getButtonSaveScanResults().setEnabled(newContent.length() > 0);
    }

    public void refresScanResultsForMap() {
        GUIStaticUtils.setWaitCursor(this, true);
        StringBuilder stringBuilderAll = dumAllCustomers();
        setText(stringBuilderAll.toString());
        GUIStaticUtils.setWaitCursor(this, false);
    }

    private StringBuilder dumAllCustomers() {
        StringBuilder stringBuilderAll = new StringBuilder();
        testCustomerMap.entrySet().forEach(testCustomerEntry -> {
            TestCustomer testCustomer = testCustomerEntry.getValue();
            StringBuilder stringBuilder = new StringBuilder();
            testCustomer.dumpResults(stringBuilder, "\n");
            if (stringBuilder.length() > 0) {
                stringBuilderAll.append(stringBuilder);
            }
        });
        return stringBuilderAll;
    }

    private void refresScanResultsForTestCustomer(TestCustomer testCustomer) {
        StringBuilder stringBuilder = new StringBuilder();
        testCustomer.dumpResults(stringBuilder, "\n");
        setText(stringBuilder.toString());
    }

    private void refresScanResultsForTestScenario(String command, TestScenario testScenario) {
        StringBuilder stringBuilder = new StringBuilder();
        testScenario.dumpResults(command, stringBuilder, "\n");
        setText(stringBuilder.toString());
    }

    private void refresScanResultsForCommand(ScanResults scanResults, TestCustomer testCustomer) {
        //        scanResults.dumpResults(stringBuilder, "\n");
//        testCustomer.getTestScenariosMap().entrySet().forEach(testScenarioEntry -> {
//            TestScenario testScenario = testScenarioEntry.getValue();
//            testScenario.dumpResults(ScanResultsForCommand.getCommand(), stringBuilder, "\n");
//        });
        setText("");
    }

    private MouseInputAdapter doDoubClickOnTree() {
        return new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                if (mouseEvent.getClickCount() == 2) {
                    DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) getTreeCustomers().getLastSelectedPathComponent();
                    if (selectedNode != null && selectedNode instanceof DifferenceTreeNode) {
//                        getButtonStartDifTool().setEnabled(diffenrenceInfo != null);
                        doStartDiffTool();
                    }
                }
            }
        };
    }

    @Override
    public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
        final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) getTreeCustomers().getLastSelectedPathComponent();
        if (selectedNode == null) {
            return;
        }
        JPanel theCardPanel = (JPanel) getSplitPanelTreeView().getRightComponent();
        theCardPanel.removeAll();
        String key = selectedNode.toString();
        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selectedNode.getParent();
        Object userObject = selectedNode.getUserObject();
        currentCardView = cardViewsMap.get(key);
        if (currentCardView == null) {
            if (userObject instanceof Map) {
                currentCardView = new ScanResultDefaultView(environmentConfig, treeCustomers);
            } else if (userObject instanceof TestCustomer) {
                currentCardView = new ScanResultDefaultView(environmentConfig, treeCustomers);
            } else if (userObject instanceof TestScenario) {
                currentCardView = new ScanResultDefaultView(environmentConfig, treeCustomers);
            } else if (userObject instanceof String) {
                currentCardView = new ScanResultDefaultView(environmentConfig, treeCustomers);
                cardViewsMap.put(key, currentCardView);
            }
            if (userObject instanceof Map) {
                refresScanResultsForMap();
            } else if (userObject instanceof TestCustomer) {
                TestCustomer testCustomer = (TestCustomer) userObject;
                refresScanResultsForTestCustomer(testCustomer);
            } else if (userObject instanceof TestScenario) {
                String command = (String) parentNode.getUserObject();
                TestScenario testScenario = (TestScenario) userObject;
                refresScanResultsForTestScenario(command, testScenario);
            } else if (userObject instanceof String) {
                TestCustomer testCustomer = (TestCustomer) parentNode.getUserObject();
                ScanResults scanResults = new ScanResults();
                refresScanResultsForCommand(scanResults, testCustomer);
            }
            theCardPanel.add(key, currentCardView);
            int dividerLocation = getSplitPanelTreeView().getDividerLocation();
            CardLayout cardLayout = (CardLayout) (theCardPanel.getLayout());
            cardLayout.show(theCardPanel, key);
            getSplitPanelTreeView().setDividerLocation(dividerLocation);
        }

    }
}