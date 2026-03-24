package de.creditreform.crefoteam.cte.tesun.gui.logsearch;

import java.io.FileNotFoundException;
import java.net.URL;

import de.creditreform.crefoteam.cte.tesun.gui.logsearch.model.LogFilesTableModel;
import de.creditreform.crefoteam.cte.tesun.gui.logsearch.model.SearchResultsTableModel;
import de.creditreform.crefoteam.cte.tesun.gui.logsearch.view.LogFilesView;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

import de.creditreform.crefoteam.cte.tesun.gui.BaseGUITest;
import de.creditreform.crefoteam.cte.tesun.gui.utils.GUIStaticUtils;
import de.creditreform.crefoteam.cte.tesun.logsearch.AbstractSearchableLogFile;
import de.creditreform.crefoteam.cte.tesun.logsearch.LogEntry;
import de.creditreform.crefoteam.cte.tesun.logsearch.SearchableLogFileFactory;

public class SearchLogsViewTest extends BaseGUITest
{
  protected ContainerOperator logFilesOp;
  protected ContainerOperator searchCriteriasOp;
  protected ContainerOperator searchResultsOp;

  LogFilesTableModel logFilesTableModel;
  JButtonOperator    buttonSearchOp;

  public SearchLogsViewTest() throws Exception {
    super(new SearchLOGsGUI());
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

    JTableOperator tableLogFilesOp = new JTableOperator( logFilesOp, new NameComponentChooser( "tableLogFiles" ) );
    logFilesTableModel = (LogFilesTableModel)tableLogFilesOp.getModel();
    logFilesTableModel.clearTable();

    buttonSearchOp = new JButtonOperator( searchResultsOp, new NameComponentChooser( "buttonSearch" ) );
    Assert.assertEquals( buttonSearchOp.isEnabled(), false );
  }

  @After
  public void tearDown()
  {
    super.tearDown();
  }

  private void addFileToModel( String fileName, boolean activated ) throws FileNotFoundException
  {
    final URL resourceURL = this.getClass().getResource( fileName );
    AbstractSearchableLogFile logFile = (AbstractSearchableLogFile)SearchableLogFileFactory.createInstanceFor( resourceURL.getPath() );
    logFile.setActivated( activated );
    logFilesTableModel.addRow( logFile );
  }

  @Test
  public void testLogFilesTableModelChanges() throws FileNotFoundException
  {
    Assert.assertEquals( buttonSearchOp.isEnabled(), false );

    addFileToModel( "/all_flux_out.log", true );
    Assert.assertEquals( buttonSearchOp.isEnabled(), true );

    AbstractSearchableLogFile logFile = (AbstractSearchableLogFile)logFilesTableModel.getRow( 0 );
    logFile.setActivated( false );
    logFilesTableModel.fireTableDataChanged();
    Assert.assertEquals( buttonSearchOp.isEnabled(), false );

    addFileToModel( "/all_flux_out.log.1.gz", false );
    Assert.assertEquals( buttonSearchOp.isEnabled(), false );
    addFileToModel( "/all_flux_out.log.2.gz", true );
    Assert.assertEquals( buttonSearchOp.isEnabled(), true );
  }

  // TODO geht nicht!!! @Test
  public void testSearchForLogFiles() throws FileNotFoundException
  {
    Assert.assertEquals( buttonSearchOp.isEnabled(), false );
    addFileToModel( "/all_flux_out.log", true );
    addFileToModel( "/all_flux_out.log.1.gz", true );
    addFileToModel( "/all_flux_out.log.2.gz", true );
    Assert.assertEquals( buttonSearchOp.isEnabled(), true );
    searchAndTest(357); // ALL

    // Suchkriterien ändern...
    JComboBoxOperator  comboBoxSearchCritsTypeOp = new JComboBoxOperator( searchCriteriasOp, new NameComponentChooser( "comboBoxSearchCritsType" ) );
    JTextFieldOperator textFieldSearchCritsFromOp = new JTextFieldOperator( searchCriteriasOp, new NameComponentChooser( "textFieldSearchCritsFrom" ) );
    JTextFieldOperator textFieldSearchCritsToOp = new JTextFieldOperator( searchCriteriasOp, new NameComponentChooser( "textFieldSearchCritsTo" ) );
    JTextFieldOperator textFieldSearchCritsPackageOp = new JTextFieldOperator( searchCriteriasOp, new NameComponentChooser( "textFieldSearchCritsPackage" ) );
    JTextFieldOperator textFieldSearchCritsInfoOp = new JTextFieldOperator( searchCriteriasOp, new NameComponentChooser( "textFieldSearchCritsInfo" ) );

    comboBoxSearchCritsTypeOp.setSelectedItem( LogEntry.ENTRY_TYPE.INFO );
    searchAndTest(325);
    comboBoxSearchCritsTypeOp.setSelectedItem( LogEntry.ENTRY_TYPE.WARN);
    searchAndTest(26);
    comboBoxSearchCritsTypeOp.setSelectedItem( LogEntry.ENTRY_TYPE.ERROR );
    searchAndTest(4);
    comboBoxSearchCritsTypeOp.setSelectedItem( LogEntry.ENTRY_TYPE.FATAL );
    searchAndTest(2);

    comboBoxSearchCritsTypeOp.setSelectedItem( LogEntry.ENTRY_TYPE.INFO);
    enterText(textFieldSearchCritsFromOp, "22.06.2015 11:56:10");
    searchAndTest(318);

    enterText(textFieldSearchCritsToOp, "23.06.2015 14:00:00" );
    searchAndTest(141);
    
    enterText(textFieldSearchCritsFromOp, "*" );
    enterText(textFieldSearchCritsToOp, "*" );
    enterText(textFieldSearchCritsPackageOp, "de.creditreform.crefoteam.ctertnexport.rtnexport" );
    searchAndTest(8);
    
    enterText(textFieldSearchCritsPackageOp, "de.creditreform.crefoteam.statusdispatchprocessing" );
    searchAndTest(26);

    enterText(textFieldSearchCritsPackageOp, "" );
    enterText(textFieldSearchCritsInfoOp, "RuntimeException beim XML-Export der Crefo 6250301832" );
    searchAndTest(0);
    comboBoxSearchCritsTypeOp.setSelectedItem( LogEntry.ENTRY_TYPE.ERROR );
    searchAndTest(1);
  }

  private void enterText( JTextFieldOperator textFieldOp, String strContent )
  {
    textFieldOp.enterMouse();
    textFieldOp.enterText( strContent );
    textFieldOp.exitMouse();
    textFieldOp.typeKey( '\t' );
  }

  private void searchAndTest(int expectedSofsCount)
  {
    buttonSearchOp.push();
    GUIStaticUtils.warteBisken( 100 );
    JTableOperator searchResultsTableOp = new JTableOperator( searchResultsOp, new NameComponentChooser( "tableSearchResults" ) );
    SearchResultsTableModel searchResultsTableModel = (SearchResultsTableModel)searchResultsTableOp.getModel();
    Assert.assertEquals( searchResultsTableModel.getRowCount(), expectedSofsCount);
  }

}
