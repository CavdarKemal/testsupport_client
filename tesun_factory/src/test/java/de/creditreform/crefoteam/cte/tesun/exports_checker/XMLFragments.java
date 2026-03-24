package de.creditreform.crefoteam.cte.tesun.exports_checker;

import de.creditreform.crefoteam.cte.tesun.util.TesunUtilites;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLFragments
{

  public static final String XMLFRAGMENT = "" + //
  "  <update-message>" + //
  "      <update-on-company>" + //
  "        <crefo-number>4110100864</crefo-number>" + //
  "        <identification-number>04114110100864</identification-number>" + //
  "        <company-status>" + //
  "            <key>PEST-4</key>" + //
  "            <designation>erloschen</designation>" + //
  "        </company-status>" + //
  "        <to-be-deleted>false</to-be-deleted>" + //
  "        <advertising-prohibited>false</advertising-prohibited>" + //
  "        <company-identification>" + //
  "            <mailing-address>" + //
  "                <name1>ARNAFELL Verwaltungs GmbH &amp;</name1>" + //
  "                <name2>Co. KG</name2>" + //
  "                <address-for-service>" + //
  "                    <country>" + //
  "                        <key>DE</key>" + //
  "                        <designation>Deutschland</designation>" + //
  "                    </country>" + //
  "                    <postcode>58099</postcode>" + //
  "                    <city>Hagen</city>" + //
  "                    <quarter>Boele</quarter>" + //
  "                    <street>Kabeler Str.</street>" + //
  "                    <house-number>510</house-number>" + //
  "                </address-for-service>" + //
  "            </mailing-address>" + //
  "            <company-name>ARNAFELL Verwaltungs GmbH &amp; Co. KG</company-name>" + //
  "            <legal-form>" + //
  "                <key>LEFO-DE-900</key>" + //
  "                <designation>GmbH &amp; Co. KG</designation>" + //
  "            </legal-form>" + //
  "            <register-id>101332</register-id>" + //
  "            <register-type>" + //
  "                <key>RETY-DE-1</key>" + //
  "                <designation>HRA</designation>" + //
  "            </register-type>" + //
  "            <register-court>" + //
  "                <court>Amtsgericht</court>" + //
  "                <postcode>58097</postcode>" + //
  "                <city>Hagen</city>" + //
  "            </register-court>" + //
  "        </company-identification>" + //
  "        <company-facilities>" + //
  "            <legal-office>" + //
  "                <address-range>" + //
  "                    <key>ADRG-2</key>" + //
  "                    <designation>Juristischer Sitz</designation>" + //
  "                </address-range>" + //
  "                <mailing-address>" + //
  "                    <address-for-service>" + //
  "                        <country>" + //
  "                            <key>DE</key>" + //
  "                            <designation>Deutschland</designation>" + //
  "                        </country>" + //
  "                        <postcode>58099</postcode>" + //
  "                        <city>Hagen</city>" + //
  "                        <quarter>Boele</quarter>" + //
  "                        <street>Kabeler Str.</street>" + //
  "                        <house-number>510</house-number>" + //
  "                    </address-for-service>" + //
  "                </mailing-address>" + //
  "            </legal-office>" + //
  "        </company-facilities>" + //
  "    </update-on-company>" + //
  "  </update-message>" + //
  "";

  public static Document XML_DOCUMENT;
  static
  {
    try
    {
      XML_DOCUMENT = stringToDom( XMLFRAGMENT );
    }
    catch( Exception ex )
    {
      ex.printStackTrace();
    }
  }

  public static Document stringToDom( String xmlSource ) throws SAXException, ParserConfigurationException, IOException
  {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder        builder = factory.newDocumentBuilder();
    return builder.parse( new InputSource( new StringReader( xmlSource ) ) );
  }

  public static Document cloneDocument( Document oDocument ) throws TransformerException
  {
    TransformerFactory tfactory = TransformerFactory.newInstance();
    Transformer        tx       = tfactory.newTransformer();
    DOMSource          source   = new DOMSource( oDocument );
    DOMResult          result   = new DOMResult();
    tx.transform( source, result );
    return ( Document )result.getNode();
  }

  public static void modifyXMLDocument( Document xmlDocument, String[] xpathExpressions, String[] newValues ) throws SAXException, ParserConfigurationException, IOException, TransformerException, XPathExpressionException
  {
    final XPath xpath  = XPathFactory.newInstance().newXPath();
    int         nIndex = 0;
    for( String newValue : newValues )
    {
      String xpathExpression = xpathExpressions[nIndex++];
      if( newValue == null )
      {
        continue;
      }
      // System.out.print( "XPath: '" + xpathExpression );
      NodeList nodes = ( NodeList )xpath.evaluate( xpathExpression, xmlDocument, XPathConstants.NODESET );
      // System.out.println( "' liefert " + nodes.getLength() + " XML-Nodes." );
      for( int i = 0, len = nodes.getLength(); i < len; i++ )
      {
        Node node = nodes.item( i );
        // System.out.println( "\tSetze den neuen Wert '" + newValue + "' für Node '" + node.getNodeName() + "'" );
        node.setTextContent( newValue );
      }
    }
  }

  public static String getModifiedXMLContent( String[] xpathExpressions, String[] newValues ) throws Exception
  {
    if( newValues.length < 1 ) {
      return XMLFRAGMENT;
    }
    Document xmlDocument = XMLFragments.cloneDocument( XMLFragments.XML_DOCUMENT );
    modifyXMLDocument( xmlDocument, xpathExpressions, newValues );
    final String prettyString = TesunUtilites.toPrettyString(xmlDocument, 2);
    return prettyString;
  }

  public static void dumpXPathsFromDocument( Document xmlDocument, String[] xpathExpressions ) throws XPathExpressionException
  {
    final XPath xpath = XPathFactory.newInstance().newXPath();
    for( String xpathExpression : xpathExpressions )
    {
      System.out.print( "XPath: '" + xpathExpression );
      NodeList nodes = ( NodeList )xpath.evaluate( xpathExpression, xmlDocument, XPathConstants.NODESET );
      System.out.println( "' liefert " + nodes.getLength() + " XML-Nodes." );
      for( int i = 0, len = nodes.getLength(); i < len; i++ )
      {
        Node node = nodes.item( i );
        System.out.println( "\tNode '" + node.getNodeName() + "' hat den Wert '" + node.getTextContent() + "'" );
      }
    }
  }

}
