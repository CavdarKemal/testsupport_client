package de.creditreform.crefoteam.cte.tesun.gui.xmleditor;

import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class XmlEditor extends JFrame
{

  public static void main( String[] args )
  {
    XmlEditor xmlEditor = new XmlEditor();
    xmlEditor.setVisible( true );
  }

  public XmlEditor()
  {

    super( "XML Text Editor Demo" );
    setSize( 800, 600 );

    JPanel panel = new JPanel();
    panel.setLayout( new GridLayout() );

    XmlTextPane xmlTextPane = new XmlTextPane();
    panel.add( xmlTextPane );

    add( panel );
  }
}
