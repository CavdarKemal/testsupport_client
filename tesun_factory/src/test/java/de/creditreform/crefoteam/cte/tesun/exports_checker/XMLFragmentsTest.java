package de.creditreform.crefoteam.cte.tesun.exports_checker;

import org.junit.Test;
import org.w3c.dom.Document;

public class XMLFragmentsTest
{

  public static final String[] XPATH_EXPRESSIONS = new String[]
  {
    "/update-message[1]/update-on-company[1]/company-identification[1]/mailing-address[1]/address-for-service[1]/country[1]/key[1]/text()[1]",
    "/update-message[1]/update-on-company[1]/company-identification[1]/register-type[1]/key[1]/text()[1]",
    "/update-message[1]/update-on-company[1]/company-facilities[1]/legal-office[1]/address-range[1]/designation[1]/text()[1]",
    "/update-message[1]/update-on-company[1]/company-identification[1]/mailing-address[1]/address-for-service[1]/postcode[1]/text()[1]",
    "/update-message[1]/update-on-company[1]/company-identification[1]/register-court[1]/court[1]/text()[1]",
    "/update-message[1]/update-on-company[1]/company-identification[1]/register-court[1]/postcode[1]/text()[1]",
  };


  @Test public void testXMLFragments1() throws Exception
  {
    String XMLFRAGMENT0 = "" + //
    "<Employees>" + //
    "    <Employee emplid=\"1111\" type=\"admin\">" + //
    "        <firstname>John</firstname>" + //
    "        <lastname>Watson</lastname>" + //
    "        <age>30</age>" + //
    "        <email>johnwatson@sh.com</email>" + //
    "    </Employee>" + //
    "    <Employee emplid=\"2222\" type=\"admin\">" + //
    "        <firstname>Sherlock</firstname>" + //
    "        <lastname>Homes</lastname>" + //
    "        <age>32</age>" + //
    "        <email>sherlock@sh.com</email>" + //
    "    </Employee>" + //
    "    <Employee emplid=\"3333\" type=\"user\">" + //
    "        <firstname>Jim</firstname>" + //
    "        <lastname>Moriarty</lastname>" + //
    "        <age>52</age>" + //
    "        <email>jim@sh.com</email>" + //
    "    </Employee>" + //
    "    <Employee emplid=\"4444\" type=\"user\">" + //
    "        <firstname>Mycroft</firstname>" + //
    "        <lastname>Holmes</lastname>" + //
    "        <age>41</age>" + //
    "        <email>mycroft@sh.com</email>" + //
    "    </Employee>" + //
    "</Employees>";

    Document theDocument0  = XMLFragments.stringToDom( XMLFRAGMENT0 );
    String[] xpathElements = new String[]
    {
      "/Employees/Employee[@emplid='3333']/email",
      "/Employees/Employee[1]/firstname",
      "/Employees/Employee[3]/firstname",
      "/Employees/Employee[@emplid='2222']",
      "/Employees/Employee[@type='admin']/firstname",
      "/Employees/Employee[age>40]/firstname",
    };
    XMLFragments.modifyXMLDocument( theDocument0, xpathElements, //
                                    new String[]
                                    {
                                      "aaa@bbb.de", "Kemal", "Gubidin", "345", "Abidin", "Franklin",
                                    } );
    XMLFragments.dumpXPathsFromDocument( theDocument0, xpathElements );
  }

  @Test public void testXMLFragments2() throws Exception
  {
    Document theDocument1   = XMLFragments.cloneDocument( XMLFragments.XML_DOCUMENT );
    String[] xpathElements1 = new String[]
    {
      "/update-message/update-on-company/crefo-number",
      "/update-message/update-on-company/company-identification/mailing-address/address-for-service/country/key",
      "/update-message/update-on-company/company-identification/register-type/key",
      "/update-message/update-on-company/company-facilities/legal-office/address-range/designation",
    };
    XMLFragments.modifyXMLDocument( theDocument1, xpathElements1, //
                                    new String[] { "1111111111", "FR", "RETY-DE-2", "Juristischer  Stand" } );
    XMLFragments.dumpXPathsFromDocument( theDocument1, xpathElements1 );
  }

  @Test public void testXMLFragments3() throws Exception
  {
    String[] xpathElements1     = new String[]
    {
      "/update-message/update-on-company/crefo-number",
      "/update-message/update-on-company/company-identification/mailing-address/address-for-service/country/key",
      "/update-message/update-on-company/company-identification/register-type/key",
      "/update-message/update-on-company/company-facilities/legal-office/address-range/designation",
    };
    String   modifiedXMLContent = XMLFragments.getModifiedXMLContent( xpathElements1, //
                                                                      new String[] { "1111111111", "FR", "RETY-DE-2", "Juristischer  Stand" } );
    System.out.println( modifiedXMLContent );
  }
}
