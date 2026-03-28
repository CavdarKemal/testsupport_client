package de.creditreform.crefoteam.cte.tesun.gui.view;

import de.creditreform.crefoteam.cte.tesun.gui.design.TestSupportMainTabPabel;

public class TestSupportMainTabView extends TestSupportMainTabPabel {

    public TestSupportMainTabView() {
        super();
        initListeners();
    }

    private void initListeners() {
        getButtonClearLOGPanel().addActionListener(e -> getTextAreaTaskListenerInfo().setText(""));
    }
}
