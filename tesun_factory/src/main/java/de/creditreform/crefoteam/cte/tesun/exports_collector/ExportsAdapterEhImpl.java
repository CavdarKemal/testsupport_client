package de.creditreform.crefoteam.cte.tesun.exports_collector;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;

import javax.naming.InsufficientResourcesException;
import java.util.Arrays;
import java.util.List;

public class ExportsAdapterEhImpl extends ExportsAdapterDefImpl {

   public ExportsAdapterEhImpl(ExportsAdapterConfig exportsAdapterConfig, TestCustomer testCustomer, TesunClientJobListener tesunClientJobListener) throws InsufficientResourcesException {
      super(exportsAdapterConfig, testCustomer, tesunClientJobListener);
   }

   @Override
   public List<String> getZipFilePrefixes() {
      return Arrays.asList("abCrefo", "abFTN_");
   }
}
