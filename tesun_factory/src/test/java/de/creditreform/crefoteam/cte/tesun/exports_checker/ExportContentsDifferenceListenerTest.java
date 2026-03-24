package de.creditreform.crefoteam.cte.tesun.exports_checker;

import java.util.List;

import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.NodeDetail;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

public class ExportContentsDifferenceListenerTest
{
  private static final String IGNOIEREN   = "Der Unterschied hätte ignoriert werden müssen!";
  private static final String AKZEPTIEREN = "Der Unterschied hätte akzeptiert werden müssen!";

  private Difference createDifferenceMockForValue( String strType, String strValue, String xpathLocation )
  {
    Difference mockedDifference = EasyMock.createMock( Difference.class );
    NodeDetail testDetail = new NodeDetail( strValue, null, xpathLocation );
    EasyMock.expect( mockedDifference.getDescription() ).andReturn( strType );
    EasyMock.expect( mockedDifference.getTestNodeDetail() ).andReturn( testDetail );
    EasyMock.replay( mockedDifference );
    return mockedDifference;
  }

  private Difference createDifferenceMockForTextValue( String strValue, String xpathLocation )
  {
    return createDifferenceMockForValue( "text value", strValue, xpathLocation );
  }

  private Difference createDifferenceMockForAttributeValue( String strValue, String xpathLocation )
  {
    return createDifferenceMockForValue( "attribute value", strValue, xpathLocation );
  }

  @Test
  public void testForNumberOfChildNodesDifferences()
  {
    Difference mockedDifference = null;
    int differenceFound = 0;

    List<String> ignorablePathsList = TestFallCheckRefExports.parseIgnorablePaths( "" );
    ExportContentsDifferenceListener cut = new ExportContentsDifferenceListener( ignorablePathsList );

    mockedDifference = createDifferenceMockForValue( "number of child nodes", "", "" );
    differenceFound = cut.differenceFound( mockedDifference );
    Assert.assertEquals( AKZEPTIEREN, differenceFound, DifferenceListener.RETURN_ACCEPT_DIFFERENCE );
    EasyMock.verify( mockedDifference );
  }
  
  @Test
  public void testForTextValueDifferences()
  {
    Difference mockedDifference = null;
    int differenceFound = 0;
    String strIgnorableXPaths = "/fremder-vc[1];" + //
                                "/steuerungsdaten[1]/eigner-vc[1];" + //
                                "/rtn-firmendaten[1]/steuerungsdaten[1]/crefonummer[1];";

    List<String> ignorablePathsList = TestFallCheckRefExports.parseIgnorablePaths( strIgnorableXPaths );
    ExportContentsDifferenceListener cut = new ExportContentsDifferenceListener( ignorablePathsList );

    mockedDifference = createDifferenceMockForTextValue( "4110104137", "/rtn-firmendaten[1]/steuerungsdaten[1]/fremder-vc[1]/text()[1]" );
    differenceFound = cut.differenceFound( mockedDifference );
    Assert.assertEquals( IGNOIEREN, differenceFound, DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL );
    EasyMock.verify( mockedDifference );

    mockedDifference = createDifferenceMockForTextValue( "4110104137", "/rtn-firmendaten[1]/steuerungsdaten[1]/eigner-vc[1]/text()[1]" );
    differenceFound = cut.differenceFound( mockedDifference );
    Assert.assertEquals( IGNOIEREN, differenceFound, DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL );
    EasyMock.verify( mockedDifference );

    mockedDifference = createDifferenceMockForTextValue( "4110104137", "/rtn-firmendaten[1]/steuerungsdaten[1]/crefonummer[1]/text()[1]" );
    differenceFound = cut.differenceFound( mockedDifference );
    Assert.assertEquals( IGNOIEREN, differenceFound, DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL );
    EasyMock.verify( mockedDifference );

    mockedDifference = createDifferenceMockForTextValue( "4110104137", "/rtn-firmendaten[1]/steuerungsdaten[1]/firmenstatus[1]/text()[1]" );
    differenceFound = cut.differenceFound( mockedDifference );
    Assert.assertEquals( AKZEPTIEREN, differenceFound, DifferenceListener.RETURN_ACCEPT_DIFFERENCE );
    EasyMock.verify( mockedDifference );

  }

  @Test
  public void testForAttributeValueDifferences()
  {
    Difference mockedDifference = null;
    int differenceFound = 0;
    String strIgnorableXPaths = "/datum-gruendung2[1]/@monat;" + //
                                "/rtn-firmendatenexport[1]/rtn-firmendaten[1]/firma-rechtsform[1]/datum-gruendung2[1]/@jahr";

    List<String> ignorablePathsList = TestFallCheckRefExports.parseIgnorablePaths( strIgnorableXPaths );
    ExportContentsDifferenceListener cut = new ExportContentsDifferenceListener( ignorablePathsList );

    mockedDifference = createDifferenceMockForAttributeValue( "10", "/rtn-firmendatenexport[1]/rtn-firmendaten[1]/firma-rechtsform[1]/datum-gruendung2[1]/@monat" );
    differenceFound = cut.differenceFound( mockedDifference );
    Assert.assertEquals( IGNOIEREN, differenceFound, DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL );
    EasyMock.verify( mockedDifference );

    mockedDifference = createDifferenceMockForAttributeValue( "2012", "/rtn-firmendatenexport[1]/rtn-firmendaten[1]/firma-rechtsform[1]/datum-gruendung2[1]/@jahr" );
    differenceFound = cut.differenceFound( mockedDifference );
    Assert.assertEquals( IGNOIEREN, differenceFound, DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL );
    EasyMock.verify( mockedDifference );

    mockedDifference = createDifferenceMockForAttributeValue( "23", "/rtn-firmendatenexport[1]/rtn-firmendaten[1]/firma-rechtsform[1]/datum-gruendung2[1]/@tag" );
    differenceFound = cut.differenceFound( mockedDifference );
    Assert.assertEquals( AKZEPTIEREN, differenceFound, DifferenceListener.RETURN_ACCEPT_DIFFERENCE );
    EasyMock.verify( mockedDifference );

  }
}
