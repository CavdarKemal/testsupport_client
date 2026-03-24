package de.creditreform.crefoteam.cte.tesun.gui.utils;

import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class XmlViewFactory implements ViewFactory {

    public View create(Element element) {
        return new XmlView(element);
    }

}
