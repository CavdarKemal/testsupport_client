package de.creditreform.crefoteam.cte.tesun.exports_collector;

import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigInfo;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.propertyfiles.PathInfo;

import javax.naming.InsufficientResourcesException;
import java.util.HashMap;
import java.util.Map;

public class ExportsAdapterFactory {
   private final Map<String, ExportsAdapter> exportsAdapterMap = new HashMap<>();

   public ExportsAdapterFactory(TesunConfigInfo tesunConfigInfo, Map<String, TestCustomer> activeCustomersMap, TesunClientJobListener tesunClientJobListener) throws InsufficientResourcesException {
      for (Map.Entry<String, TestCustomer> entrySet : activeCustomersMap.entrySet()) {
         TestCustomer testCustomer = entrySet.getValue();
         String customerKey = testCustomer.getCustomerKey().toUpperCase();
         ExportsAdapterConfig exportsAdapterConfig = new ExportsAdapterConfig(tesunConfigInfo, customerKey);
         if (customerKey.equalsIgnoreCase("EH")) {
            exportsAdapterMap.put(customerKey, new ExportsAdapterEhImpl(exportsAdapterConfig, testCustomer, tesunClientJobListener));
         } else if (customerKey.equalsIgnoreCase("VSD")
                 || customerKey.equalsIgnoreCase("VSH")
                 || customerKey.equalsIgnoreCase("VSO")) {
            exportsAdapterMap.put(customerKey, new ExportsAdapterVsxImpl(exportsAdapterConfig, testCustomer, tesunClientJobListener));
         } else {
            exportsAdapterMap.put(customerKey, new ExportsAdapterDefImpl(exportsAdapterConfig, testCustomer, tesunClientJobListener));
         }
      }
   }

   public ExportsAdapter findExportsAdapter(PathInfo baseDirKey) {
      return exportsAdapterMap.get(baseDirKey.getCurtomerKey().toUpperCase());
   }

   public ExportsAdapter findExportsAdapter(String customerKey) {
      return exportsAdapterMap.get(customerKey.toUpperCase());
   }
}
