package de.creditreform.crefoteam.cte.tesun.xmlsearch.config;

import com.google.inject.Binder;
import com.google.inject.Module;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.XmlMatcherWrapperFactoryCrefoExport;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.handler.ExportedZipFilesHandler;

public class XmlSearchModule implements Module
{
  public XmlSearchModule()
  {
    super();
  }

  @Override public void configure( Binder binder )
  {
    binder.bind( ExportedZipFilesHandler.class).toInstance( new ExportedZipFilesHandler(new XmlMatcherWrapperFactoryCrefoExport()) );
  }

}
