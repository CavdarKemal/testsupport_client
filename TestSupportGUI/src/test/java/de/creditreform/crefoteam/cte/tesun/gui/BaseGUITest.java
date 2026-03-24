package de.creditreform.crefoteam.cte.tesun.gui;

import java.awt.Component;
import java.awt.Container;
import java.io.File;

import javax.swing.JOptionPane;

import de.creditreform.crefoteam.cte.tesun.gui.utils.GUIFrame;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import de.creditreform.crefoteam.cte.tesun.gui.utils.GUIStaticUtils;

public class BaseGUITest
{
  protected GUIFrame guiFrame;
  protected JFrameOperator    frameOperator;
                              
  public BaseGUITest( GUIFrame guiFrame )
  {
    this.guiFrame = guiFrame;
  }
  
  @Before
  public void setUp()
  {
    frameOperator = new JFrameOperator( guiFrame );
  }
  
  @After
  public void tearDown()
  {
    guiFrame.setVisible( false );
    guiFrame.dispose();
  }

  protected GUIFrame getGuiFrame() {
    return (GUIFrame)frameOperator.getSource();
  }

  protected void ensureDialogVisible( Component component, boolean shouldBeVisible )
  {
    GUIStaticUtils.warteBisken( 30 );
    boolean isVisible = component.isVisible();
    if( shouldBeVisible )
    {
      Assert.assertTrue( isVisible );
    }
    else
    {
      Assert.assertFalse( isVisible );
    }
  }
  
  protected JDialogOperator ensureDialogVisible( String dlgTitle, boolean shouldBeVisible )
  {
    JDialogOperator dialogOperator = new JDialogOperator( dlgTitle );
    Container contentPane = dialogOperator.getContentPane();
    ensureDialogVisible( contentPane, shouldBeVisible );
    return dialogOperator;
  }
  
  protected void confirmDialog( String dilaogTitle, String strErrorMsg, String strCommand )
  {
    JDialogOperator dialogOperator = ensureDialogVisible( dilaogTitle, true );
    if( !GUIStaticUtils.isEmpty( strErrorMsg ) )
    {
      Container contentPane = dialogOperator.getContentPane();
      String strMessage = (String)( (JOptionPane)contentPane.getComponent( 0 ) ).getMessage();
      Assert.assertTrue( strMessage.contains( strErrorMsg ) );
    }
    JButtonOperator buttonOperator = new JButtonOperator( dialogOperator, strCommand );
    buttonOperator.push();
  }
  
  protected void confirmChangeDataDialog( String strCommand )
  {
    confirmDialog( "Data Change", null, strCommand );
  }
  
  protected void confirmSaveDialog( String strCommand )
  {
    JDialogOperator dialogOperator = new JDialogOperator( "Speichern" );
    ensureDialogVisible( dialogOperator.getContentPane(), true );
    JButtonOperator buttonOperator = new JButtonOperator( dialogOperator, strCommand );
    buttonOperator.push();
  }
  
  protected void confirmJFileChooser( File fileToBeChoosen, String buttonText )
  {
    JFileChooserOperator jFileChooserOperator = new JFileChooserOperator();
    ensureDialogVisible( jFileChooserOperator.getWindow(), true );
    jFileChooserOperator.setSelectedFile( fileToBeChoosen );
    JButtonOperator buttonOperatorOpen = new JButtonOperator( jFileChooserOperator, buttonText );
    buttonOperatorOpen.push();
  }
  
}
