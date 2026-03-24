package de.creditreform.crefoteam.cte.tesun.gui;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import de.creditreform.crefoteam.cte.testutils_cte.junit4rules.logappenders.AppenderRuleSystemOut;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JarResourcesTest {

   Logger logger;

   @Rule
   public AppenderRuleSystemOut ruleSystemOut = new AppenderRuleSystemOut();

   @Before
   public void setUp() {
      logger = LoggerFactory.getLogger(getClass());
   }

   public static List<String> getJarContent(String jarPath) throws IOException {
      List<String> stringList = new ArrayList<>();
      JarFile jarFile = new JarFile(jarPath);
      Enumeration<JarEntry> entries = jarFile.entries();
      while (entries.hasMoreElements()) {
         JarEntry jarEntry = entries.nextElement();
         String jarEntryName = jarEntry.getName();
         stringList.add(jarEntryName);
      }
      return stringList;
   }

   private String getJarFileNameForClass(Class theClass) {
      URL url = theClass.getProtectionDomain().getCodeSource().getLocation();
      return url.getPath();
   }

   @Test
   public void testGetAllResourcesFromClassPath() throws Exception {
      ClassPath cp= ClassPath.from(Thread.currentThread().getContextClassLoader());
      logger.info("JAR-Content vom CLASSPATH {}");
      final ImmutableSet<ClassPath.ResourceInfo> resources = cp.getResources();
      for(ClassPath.ResourceInfo resourceInfo : resources) {
         logger.info(resourceInfo.getResourceName());
      }
   }

}
