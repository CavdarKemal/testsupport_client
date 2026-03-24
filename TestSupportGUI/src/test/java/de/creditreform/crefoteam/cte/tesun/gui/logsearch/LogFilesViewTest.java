package de.creditreform.crefoteam.cte.tesun.gui.logsearch;

import java.io.File;
import java.net.URL;

import de.creditreform.crefoteam.cte.tesun.gui.logsearch.view.LogFilesView;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

import de.creditreform.crefoteam.cte.tesun.gui.BaseGUITest;
import de.creditreform.crefoteam.cte.tesun.gui.logsearch.model.LogFilesTableModel;

public class LogFilesViewTest extends BaseGUITest
{
  
  protected ContainerOperator logFilesOp;
  protected ContainerOperator searchCriteriasOp;
  protected ContainerOperator searchResultsOp;
                              
  JTableOperator              tableLogFilesOp;
  JButtonOperator             buttonAddOp;
  JButtonOperator             buttonRemoveOp;
  JButtonOperator             buttonSelectAllOp;
  JButtonOperator             buttonSelectNoneOp;
  JButtonOperator             buttonInvertSelectionOp;
  LogFilesTableModel model;
                              
  public LogFilesViewTest() throws Exception {
    super( new SearchLOGsGUI() );
  }
  
  @Before
  public void setUp()
  {
    super.setUp();
    
    logFilesOp = new ContainerOperator( frameOperator, new NameComponentChooser( "logFilesView" ) );
    LogFilesView logFilesView = (LogFilesView)logFilesOp.getSource();
    logFilesView.setGuiFrame(getGuiFrame());
    searchCriteriasOp = new ContainerOperator( frameOperator, new NameComponentChooser( "searchCriteriasView" ) );
    searchResultsOp = new ContainerOperator( frameOperator, new NameComponentChooser( "searchResultsView" ) );
    
    tableLogFilesOp = new JTableOperator( logFilesOp, new NameComponentChooser( "tableLogFiles" ) );
    model = (LogFilesTableModel)tableLogFilesOp.getModel();
    Assert.assertTrue( model.getRowCount() == 0 );
    buttonAddOp = new JButtonOperator( logFilesOp, new NameComponentChooser( "buttonAdd" ) );
    Assert.assertTrue( buttonAddOp.isEnabled() );
    buttonRemoveOp = new JButtonOperator( logFilesOp, new NameComponentChooser( "buttonRemove" ) );
    Assert.assertFalse( buttonRemoveOp.isEnabled() );
    buttonSelectAllOp = new JButtonOperator( logFilesOp, new NameComponentChooser( "buttonSelectAll" ) );
    Assert.assertFalse( buttonSelectAllOp.isEnabled() );
    buttonSelectNoneOp = new JButtonOperator( logFilesOp, new NameComponentChooser( "buttonSelectNone" ) );
    Assert.assertFalse( buttonSelectNoneOp.isEnabled() );
    buttonInvertSelectionOp = new JButtonOperator( logFilesOp, new NameComponentChooser( "buttonInvertSelection" ) );
    Assert.assertFalse( buttonInvertSelectionOp.isEnabled() );
  }

  @After
  public void tearDown()
  {
    if( model.getRowCount() > 0 )
    {
      removeFiles( 0, model.getRowCount() - 1, 0 );
    }
    super.tearDown();
  }
  
  private void checkControlsState()
  {
    boolean isSelected = tableLogFilesOp.getSelectedRow() >= 0;
    Assert.assertEquals( buttonRemoveOp.isEnabled(), isSelected );
    Assert.assertEquals( buttonSelectAllOp.isEnabled(), isSelected );
    Assert.assertEquals( buttonSelectNoneOp.isEnabled(), isSelected );
    Assert.assertEquals( buttonInvertSelectionOp.isEnabled(), isSelected );
  }
  
  private void addFile( String fileName, int numFiles )
  {
    buttonAddOp.push();
    final URL resourceURL = this.getClass().getResource( fileName );
    final File loadFile = new File( resourceURL.getPath() );
    confirmJFileChooser( loadFile, "Öffnen" );
    Assert.assertTrue( model.getRowCount() == numFiles );
  }
  
  private void removeFiles( int fromIistIndex, int toIistIndex, int numFiles )
  {
    tableLogFilesOp.getSelectionModel().setSelectionInterval( fromIistIndex, toIistIndex );
    buttonRemoveOp.push();
    Assert.assertTrue( model.getRowCount() == numFiles );
  }
  
  @Test
  public void testViewLogFilesAddFile()
  {
    int numFiles = 1;
    addFile( "/all_flux_out.log", numFiles++ );
    checkControlsState();
    addFile( "/all_flux_out.log.1.gz", numFiles++ );
    checkControlsState();
    addFile( "/all_flux_out.log.2.gz", numFiles++ );
    checkControlsState();
  }
  
  @Test
  public void testViewLogFilesRemoveFile()
  {
    int numFiles = 1;
    addFile( "/all_flux_out.log", numFiles++ );
    addFile( "/all_flux_out.log.1.gz", numFiles++ );
    addFile( "/all_flux_out.log.2.gz", numFiles++ );
    checkControlsState();
    
    removeFiles( 0, 0, 2 );
    checkControlsState();
    removeFiles( 0, 0, 1 );
    checkControlsState();
    removeFiles( 0, 0, 0 );
    checkControlsState();
  }
  
  @Test
  public void testViewLogFilesRemoveFiles()
  {
    int numFiles = 1;
    addFile( "/all_flux_out.log", numFiles++ );
    addFile( "/all_flux_out.log.1.gz", numFiles++ );
    addFile( "/all_flux_out.log.2.gz", numFiles++ );
    addFile( "/all_flux_out.log", numFiles++ );
    addFile( "/all_flux_out.log.1.gz", numFiles++ );
    addFile( "/all_flux_out.log.2.gz", numFiles++ );
    addFile( "/all_flux_out.log", numFiles++ );
    addFile( "/all_flux_out.log.1.gz", numFiles++ );
    addFile( "/all_flux_out.log.2.gz", numFiles++ );
    checkControlsState();
    
    removeFiles( 2, 5, 5 );
    checkControlsState();
    
    removeFiles( 1, 3, 2 );
    checkControlsState();
    
    removeFiles( 0, 1, 0 );
    checkControlsState();
    
  }
  
}
