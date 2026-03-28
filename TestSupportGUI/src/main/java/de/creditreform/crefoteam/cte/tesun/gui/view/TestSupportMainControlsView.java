package de.creditreform.crefoteam.cte.tesun.gui.view;

import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.gui.design.TestSupportMainControlsPanel;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.PropertiesException;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TestSupportMainControlsView extends TestSupportMainControlsPanel {

    private Runnable onHostChanged;
    private Runnable onRefreshEnvironment;
    private Runnable onManageJVMs;
    private Runnable onEnvironmentChanged;

    public TestSupportMainControlsView() {
        super();
    }

    public void init(TesunClientJobListener listener,
                     Runnable onHostChanged,
                     Runnable onRefreshEnvironment,
                     Runnable onManageJVMs,
                     Runnable onEnvironmentChanged) {
        this.onHostChanged = onHostChanged;
        this.onRefreshEnvironment = onRefreshEnvironment;
        this.onManageJVMs = onManageJVMs;
        this.onEnvironmentChanged = onEnvironmentChanged;
        initListeners();
    }

    private void initListeners() {
        getComboBoxEnvironment().addActionListener(e -> onEnvironmentChanged.run());
        getComboBoxActivitiHost().addActionListener(e -> onHostChanged.run());
        getComboBoxImpCycleHost().addActionListener(e -> onHostChanged.run());
        getComboBoxRestServicesHost().addActionListener(e -> onHostChanged.run());
        getButtonRefreshEnvironment().addActionListener(e -> onRefreshEnvironment.run());
        getButtonManageJVMs().addActionListener(e -> onManageJVMs.run());
    }

    public void initEnvironmentsComboBox(EnvironmentConfig currentEnvironment) {
        DefaultComboBoxModel environmentsModel = new DefaultComboBoxModel();
        Map<String, File> environmentsMap = currentEnvironment.getEnvironmentsMap();
        Iterator<String> it = environmentsMap.keySet().iterator();
        while (it.hasNext()) {
            environmentsModel.addElement(it.next());
        }
        getComboBoxEnvironment().setModel(environmentsModel);
        getComboBoxEnvironment().setSelectedItem(currentEnvironment.getCurrentEnvName());
    }

    public void initHostsFields(EnvironmentConfig currentEnvironment) throws PropertiesException {
        getComboBoxActivitiHost().setModel(new DefaultComboBoxModel());
        currentEnvironment.getRestServiceConfigsForActiviti().forEach(c ->
                getComboBoxActivitiHost().addItem(new RestInvokerConfigCbItem(c.getServiceURL(), c)));

        getComboBoxRestServicesHost().setModel(new DefaultComboBoxModel());
        currentEnvironment.getRestServiceConfigsForMasterkonsole().forEach(c ->
                getComboBoxRestServicesHost().addItem(new RestInvokerConfigCbItem(c.getServiceURL(), c)));

        getComboBoxBatchGUIHost().setModel(new DefaultComboBoxModel());
        currentEnvironment.getRestServiceConfigsForBatchGUI().forEach(c ->
                getComboBoxBatchGUIHost().addItem(new RestInvokerConfigCbItem(c.getServiceURL(), c)));

        getComboBoxImpCycleHost().setModel(new DefaultComboBoxModel());
        currentEnvironment.getRestServiceConfigsForJvmImpCycle().forEach(c ->
                getComboBoxImpCycleHost().addItem(new RestInvokerConfigCbItem(c.getServiceURL(), c)));

        getComboBoxInsoHost().setModel(new DefaultComboBoxModel());
        currentEnvironment.getRestServiceConfigsForJvmInso().forEach(c ->
                getComboBoxInsoHost().addItem(new RestInvokerConfigCbItem(c.getServiceURL(), c)));

        getComboBoxInsoBackEndHost().setModel(new DefaultComboBoxModel());
        currentEnvironment.getRestServiceConfigsForJvmInsoBackend().forEach(c ->
                getComboBoxInsoBackEndHost().addItem(new RestInvokerConfigCbItem(c.getServiceURL(), c)));
    }

    public RestInvokerConfig getSelectedActivitiConfig() {
        return ((RestInvokerConfigCbItem) getComboBoxActivitiHost().getSelectedItem()).getRestInvokerConfig();
    }

    public RestInvokerConfig getSelectedRestServicesConfig() {
        return ((RestInvokerConfigCbItem) getComboBoxRestServicesHost().getSelectedItem()).getRestInvokerConfig();
    }

    public RestInvokerConfig getSelectedImpCycleConfig() {
        return ((RestInvokerConfigCbItem) getComboBoxImpCycleHost().getSelectedItem()).getRestInvokerConfig();
    }

    public String getSelectedEnvironmentName() {
        return getComboBoxEnvironment().getSelectedItem().toString();
    }

    public void setSelectedEnvironment(String envName) {
        ActionListener[] listeners = disableCbListeners(getComboBoxEnvironment());
        getComboBoxEnvironment().setSelectedItem(envName);
        enableCbListeners(getComboBoxEnvironment(), listeners);
    }

    public List<JComponent> getComponentsToOnOff() {
        List<JComponent> list = new ArrayList<>();
        list.add(getComboBoxActivitiHost());
        list.add(getComboBoxRestServicesHost());
        list.add(getComboBoxBatchGUIHost());
        list.add(getComboBoxImpCycleHost());
        list.add(getComboBoxInsoHost());
        list.add(getComboBoxInsoBackEndHost());
        list.add(getButtonManageJVMs());
        list.add(getComboBoxEnvironment());
        list.add(getButtonRefreshEnvironment());
        return list;
    }

    public void updateAdminButtonState(EnvironmentConfig currentEnvironment) throws PropertiesException {
        getButtonManageJVMs().setEnabled(currentEnvironment.îsAdminFuncsEnabled());
    }

    private void enableCbListeners(JComboBox comboBox, ActionListener[] actionListeners) {
        for (ActionListener al : actionListeners) comboBox.addActionListener(al);
    }

    private ActionListener[] disableCbListeners(JComboBox comboBox) {
        ActionListener[] listeners = comboBox.getActionListeners();
        for (ActionListener al : listeners) comboBox.removeActionListener(al);
        return listeners;
    }

    static class RestInvokerConfigCbItem {
        private final String serviceURL;
        private final RestInvokerConfig restInvokerConfig;

        RestInvokerConfigCbItem(String serviceURL, RestInvokerConfig restInvokerConfig) {
            this.serviceURL = serviceURL;
            this.restInvokerConfig = restInvokerConfig;
        }

        public String getServiceURL() { return serviceURL; }

        public RestInvokerConfig getRestInvokerConfig() { return restInvokerConfig; }

        @Override
        public String toString() { return serviceURL; }
    }
}
