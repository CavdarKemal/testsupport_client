package de.creditreform.crefoteam.cte.tesun.gui.logsearch;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

import de.creditreform.crefoteam.cte.tesun.gui.BaseGUITest;

public class SearchCriteriasViewTest extends BaseGUITest
{
  protected ContainerOperator searchCriteriasOp;
  protected ContainerOperator searchResultsOp;

  public SearchCriteriasViewTest() throws Exception {
    super(new SearchLOGsGUI());
  }

  @Before
  public void setUp()
  {
    super.setUp();
    searchCriteriasOp = new ContainerOperator( frameOperator, new NameComponentChooser( "searchCriteriasView" ) );
    searchResultsOp = new ContainerOperator( frameOperator, new NameComponentChooser( "searchResultsView" ) );
  }

  @After
  public void tearDown()
  {
    super.tearDown();
  }

  @Test
  public void testViewSearchCriteriasInitial()
  {
    JComboBoxOperator comboBoxSearchCritsTypeOp = new JComboBoxOperator( searchCriteriasOp, new NameComponentChooser( "comboBoxSearchCritsType" ) );
    Assert.assertTrue( comboBoxSearchCritsTypeOp.getModel().getSize() == 5 );
    JTextFieldOperator textFieldSearchCritsFromOp = new JTextFieldOperator( searchCriteriasOp, new NameComponentChooser( "textFieldSearchCritsFrom" ) );
    Assert.assertTrue( textFieldSearchCritsFromOp.getText().equals( "*" ) );
    JTextFieldOperator textFieldSearchCritsToOp = new JTextFieldOperator( searchCriteriasOp, new NameComponentChooser( "textFieldSearchCritsTo" ) );
    Assert.assertTrue( textFieldSearchCritsToOp.getText().equals( "*" ) );
    JTextFieldOperator textFieldSearchCritsPackageOp = new JTextFieldOperator( searchCriteriasOp, new NameComponentChooser( "textFieldSearchCritsPackage" ) );
    Assert.assertTrue( textFieldSearchCritsPackageOp.getText().equals( "*" ) );
    JTextFieldOperator textFieldSearchCritsInfoOp = new JTextFieldOperator( searchCriteriasOp, new NameComponentChooser( "textFieldSearchCritsInfo" ) );
    Assert.assertTrue( textFieldSearchCritsInfoOp.getText().equals( "*" ) );
  }

}
