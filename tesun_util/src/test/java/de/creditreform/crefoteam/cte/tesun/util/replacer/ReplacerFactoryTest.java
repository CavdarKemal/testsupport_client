package de.creditreform.crefoteam.cte.tesun.util.replacer;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

public class ReplacerFactoryTest {
    Map<String, ReplacementMapping> replacementMappingMap = new TreeMap<>();

    @Test
    public void testReplacerFactoryInserters() {
        ReplacerFactory cut = new ReplacerFactory(Charset.defaultCharset());
        try {
            cut.insertCrefoReplacement(replacementMappingMap, 1L, 2L);
            Assert.fail("ReplacerParameterException expected! Parameter 1");
        } catch (ReplacerParameterException ex) {
            Assert.assertTrue(ex.getMessage().contains("Crefo should have 10 digits"));
            Assert.assertTrue(ex.getMessage().contains("supplied parameter: 1"));
        }
        try {
            cut.insertCrefoReplacement(replacementMappingMap, 1234567890L, 2L);
            Assert.fail("ReplacerParameterException expected! Parameter 2");
        } catch (ReplacerParameterException ex) {
            Assert.assertTrue(ex.getMessage().contains("Crefo should have 10 digits"));
            Assert.assertTrue(ex.getMessage().contains("supplied parameter: 2"));
        }
        try {
            Assert.assertEquals(cut, cut.insertCrefoReplacement(replacementMappingMap, 1234567890L, 2345678901L));
        } catch (ReplacerParameterException ex) {
            Assert.fail(ex.getMessage());
        }

        try {
            cut.insertStringReplacement("1", "2");
            Assert.fail("ReplacerParameterException expected! 1234567890<->1");
        } catch (ReplacerParameterException ex) {
            Assert.assertTrue(ex.getMessage().contains("Replacement for a longer string-to-be-replaced already exists:"));
            Assert.assertTrue(ex.getMessage().contains("1234567890<->1"));
        }
        try {
            Assert.assertEquals(cut, cut.insertStringReplacement("11", "2"));
        } catch (ReplacerParameterException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testReplacerFactory_addBiFunctionCrefoErsetzungen() {
        ReplacerFactory cut = new ReplacerFactory(Charset.defaultCharset());

        Map<Pattern, BiFunction<String, String, String>> patternMap = new HashMap<>();
        Map<String, ReplacementMapping> replacementMappingMap = new HashMap<>();
        ReplacerFactory.addBiFunctionCrefoReplacement(patternMap, replacementMappingMap);
        Assert.assertEquals("Bei leerem crefoReplacementMap dürfte kein Pattern entstehen!", 0, patternMap.size());

/* TODO REPLACEMENT
      crefoReplacementMap.put("1111111111", "999999999");
      crefoReplacementMap.put("2222222222", "888888888");
      crefoReplacementMap.put("3333333333", "777777777");
      cut.addBiFunctionCrefoReplacement(patternMap, crefoReplacementMap);
      Assert.assertEquals("Es dürfte nur ein erstes Pattern entstanden sein!",1, patternMap.size());

      Iterator<Map.Entry<Pattern, BiFunction<String, String, String>>> iterator = patternMap.entrySet().iterator();
      Map.Entry<Pattern, BiFunction<String, String, String>> firstEntry = iterator.next();
      Assert.assertEquals("Pattern falsch!","2222222222|1111111111|3333333333", firstEntry.getKey().toString());

      String s = firstEntry.toString();
      Assert.assertTrue(s.contains("1111111111"));
      Assert.assertTrue(s.contains("2222222222"));
      Assert.assertTrue(s.contains("3333333333"));

      BiFunction<String, String, String> biFunction = firstEntry.getValue();
      Assert.assertEquals("999999999", biFunction.apply("", "1111111111"));
      Assert.assertEquals("888888888", biFunction.apply("", "2222222222"));
      Assert.assertEquals("777777777", biFunction.apply("", "3333333333"));
*/
    }

    @Test
    public void testReplacerFactory_addBiFunctionEignerVCForGenerate() {
        ReplacerFactory cut = new ReplacerFactory(Charset.defaultCharset());
/*TODO REPLACEMENT
      Map<Pattern, BiFunction<String,String,String>> patternMap = new HashMap<>();
      Map<String,String> crefoReplacementMap = new HashMap<>();
      crefoReplacementMap.put("1111111111", "999999999");
      crefoReplacementMap.put("2222222222", "888888888");
      crefoReplacementMap.put("3333333333", "777777777");
      final Map<String,String> eignerVCOriginalMap = new HashMap<>();
      cut.addBiFunctionEignerVCForGenerate(patternMap, crefoReplacementMap, eignerVCOriginalMap);
      checkPatternsMap(patternMap);
*/
    }

    protected void checkPatternsMap(Map<Pattern, BiFunction<String, String, String>> patternMap) {
        Assert.assertEquals("Es dürfte nur ReplacerFactory.EIGNER_VC_PATTERNS_MAP-Pattern entstanden sein!",
                ReplacerFactory.EIGNER_VC_PATTERNS_MAP.size(), patternMap.size());
        Pattern patternAsKey = ReplacerFactory.EIGNER_VC_PATTERN_TYPE.EIGNER_CLZ_VC_PATTERN_AB30.getPattern();
        BiFunction<String, String, String> biFunction = patternMap.get(patternAsKey);
        Assert.assertNotNull(biFunction);

    }

    @Test
    public void testReplacerFactory_createForGenerate() throws IOException {
        ReplacerFactory cut = new ReplacerFactory(Charset.defaultCharset());

        String[] xmlFragmentsArray = new String[]{
                "<abf-firmendatenexport>\n" +
                        "    <abf-firmendaten>\n" +
                        "        <crefonummer>1234567891</crefonummer>\n" +
                        "        <clz-eigner-vc>123</clz-eigner-vc>\n" +
                        "    </abf-firmendaten>\n" +
                        "</abf-firmendatenexport>\n",
                "<abf-firmendatenexport>\n" +
                        "    <abf-firmendaten>\n" +
                        "        <crefonummer>4564567892</crefonummer>\n" +
                        "        <clz-eigner-vc>456</clz-eigner-vc>\n" +
                        "    </abf-firmendaten>\n" +
                        "</abf-firmendatenexport>\n",
                "<abf-firmendatenexport>\n" +
                        "    <abf-firmendaten>\n" +
                        "        <crefonummer>7894567893</crefonummer>\n" +
                        "        <clz-eigner-vc>789</clz-eigner-vc>\n" +
                        "    </abf-firmendaten>\n" +
                        "</abf-firmendatenexport>\n",
                "<abf-firmendatenexport>\n" +
                        "    <abf-firmendaten>\n" +
                        "        <crefonummer>9874567894</crefonummer>\n" +
                        "        <clz-eigner-vc>987</clz-eigner-vc>\n" +
                        "    </abf-firmendaten>\n" +
                        "</abf-firmendatenexport>\n",
                "<bdr-firmendatenexport>\n" +
                        "    <bdr-firmendaten>\n" +
                        "        <crefonummer>6544567895</crefonummer>\n" +
                        "        <clz-eigner-vc>654</clz-eigner-vc>\n" +
                        "    </bdr-firmendaten>\n" +
                        "</bdr-firmendatenexport>\n",
                "<bdr-firmendatenexport>\n" +
                        "    <bdr-firmendaten>\n" +
                        "        <crefonummer>3214567896</crefonummer>\n" +
                        "        <clz-eigner-vc>321</clz-eigner-vc>\n" +
                        "    </bdr-firmendaten>\n" +
                        "</bdr-firmendatenexport>\n",
        };
/* TODO REPLACEMENT
      // PropertyFileLoaderFunctionCollectCrefos würde folgende Map liefern...
      final Map<String , String> testFallToCrefoMap = new HashMap<>();
      testFallToCrefoMap.put("7894567893", "abf\\Relevanz_Negativ.n001");
      testFallToCrefoMap.put("9874567894", "abf\\Relevanz_Positiv.p001");
      testFallToCrefoMap.put("1234567891", "bdr\\Relevanz_Negativ.n001");
      testFallToCrefoMap.put("4564567892", "bdr\\Relevanz_Positiv.p001");

      long currentCrf = 4120035135L;
      for( Map.Entry<String, String> entry : testFallToCrefoMap.entrySet() )
      {
         long replacement = currentCrf++;
         Long theCrefo = Long.valueOf(entry.getKey());
         String newValue = entry.getValue() + ";" + replacement;
         // ... die Map muss so erweitert werden, dass <value> zusätzlich die neue Crefo getrennt mit ; enthält.
         testFallToCrefoMap.put(entry.getKey(), newValue );
         Assert.assertEquals(cut, cut.insertCrefoReplacement(theCrefo, replacement));
      }
      Integer targetEignerVC = Integer.valueOf(412);
      Assert.assertEquals(cut, cut.setEignerVCReplacementsForGenerate());

      Replacer replacer = cut.create();
      Assert.assertNotNull(replacer);

      checkreplacementForGenerate(replacer, "1234567891.xml", xmlFragmentsArray[0], "1234567891", "4120035137", targetEignerVC, "123");
      checkreplacementForGenerate(replacer, "4564567892.xml", xmlFragmentsArray[1], "4564567892", "4120035138", targetEignerVC, "456");
      checkreplacementForGenerate(replacer, "7894567893.xml", xmlFragmentsArray[2], "7894567893", "4120035135", targetEignerVC, "789");
      checkreplacementForGenerate(replacer, "9874567894.xml", xmlFragmentsArray[3], "9874567894", "4120035136", targetEignerVC, "987");
      Map<String, String> crefoReplacementMap = new HashMap<>();
      Map<String, String> eignerVCOriginalMap = replacer.getEignerVCOriginalMap();
      Assert.assertEquals(4, eignerVCOriginalMap.size());
      Assert.assertEquals("1234567891;<clz-eigner-vc;123", eignerVCOriginalMap.get("4120035137"));
      Assert.assertEquals("4564567892;<clz-eigner-vc;456", eignerVCOriginalMap.get("4120035138"));
      Assert.assertEquals("7894567893;<clz-eigner-vc;789", eignerVCOriginalMap.get("4120035135"));
      Assert.assertEquals("9874567894;<clz-eigner-vc;987", eignerVCOriginalMap.get("4120035136"));
      Map<String, TestCustomer> activeCustomersMap = new HashMap<>();
      ReplacerUtils.saveMappingFiles(new File("target"), activeCustomersMap, testFallToCrefoMap, eignerVCOriginalMap);
*/
    }

    @Test
    public void testReplacerFactory_addBiFunctionEignerVCForRestore() {
        ReplacerFactory cut = new ReplacerFactory(Charset.defaultCharset());

/* TODO REPLACEMENT
      Map<Pattern, BiFunction<String,String,String>> patternMap = new HashMap<>();
      final Map<String, String> reverseReplacementsEignerVC = new HashMap<>();
      cut.addBiFunctionEignerVCForRestore(patternMap, reverseReplacementsEignerVC);
      Assert.assertEquals("Bei leerem crefoReplacementMap dürfte kein Pattern entstehen!",0, patternMap.size());

      reverseReplacementsEignerVC.put("", "");
      cut.addBiFunctionEignerVCForRestore(patternMap, reverseReplacementsEignerVC);
      checkPatternsMap(patternMap);
*/
    }

    protected void checkreplacementForGenerate(Replacer replacer, String keyOriginalMap, String inString, String oldCrefo, String newCrefo, Integer targetEignerVC, String oldClz) {
        StringBuilder replacementB = replacer.replace(keyOriginalMap, inString);
        String replacementX = replacementB.toString();
        Assert.assertFalse(replacementX.contains(oldCrefo));
        Assert.assertTrue(replacementX.contains(newCrefo));
        Assert.assertFalse(replacementX.contains("<clz-eigner-vc>" + oldClz));
        Assert.assertTrue(replacementX.contains("<clz-eigner-vc>" + targetEignerVC.toString()));
    }

    @Test
    public void testReplacerFactory_createForRestore() {
        ReplacerFactory cut = new ReplacerFactory(Charset.defaultCharset());
        Assert.assertEquals(cut, cut.insertCrefoReplacement(replacementMappingMap, 4120035135L, 1234567891L));
        Assert.assertEquals(cut, cut.insertCrefoReplacement(replacementMappingMap, 4120035136L, 1234567892L));
        Assert.assertEquals(cut, cut.insertCrefoReplacement(replacementMappingMap, 4120035137L, 1234567893L));
        Assert.assertEquals(cut, cut.insertCrefoReplacement(replacementMappingMap, 4120035138L, 1234567894L));
        Map<String, String> reverseReplacementsEignerVC = new HashMap<>();
        reverseReplacementsEignerVC.put("4120035135", "1234567891;<clz-eigner-vc>;123");
        reverseReplacementsEignerVC.put("4120035136", "1234567892;<clz-eigner-vc>;456");
        reverseReplacementsEignerVC.put("4120035137", "1234567893;<clz-eigner-vc>;789");
        reverseReplacementsEignerVC.put("4120035138", "1234567894;<clz-eigner-vc>;987");
/* TODO REPLACEMENT
      Assert.assertEquals(cut, cut.setEignerVCReplacementsForRestore(reverseReplacementsEignerVC));
*/
        Replacer replacer = cut.create(replacementMappingMap, false);
        Assert.assertNotNull(replacer);

        String[] xmlFragmentsArray = new String[]{
                "<fsu-firmendatenexport>\n" +
                        "    <fsu-firmendaten>\n" +
                        "        <crefonummer>4120035135</crefonummer>\n" +
                        "        <clz-eigner-vc>412</clz-eigner-vc>\n" +
                        "    </fsu-firmendaten>\n" +
                        "</fsu-firmendatenexport>\n",
                "<fsu-firmendatenexport>\n" +
                        "    <fsu-firmendaten>\n" +
                        "        <crefonummer>4120035136</crefonummer>\n" +
                        "        <clz-eigner-vc>412</clz-eigner-vc>\n" +
                        "    </fsu-firmendaten>\n" +
                        "</fsu-firmendatenexport>\n",
                "<fsu-firmendatenexport>\n" +
                        "    <fsu-firmendaten>\n" +
                        "        <crefonummer>4120035137</crefonummer>\n" +
                        "        <clz-eigner-vc>412</clz-eigner-vc>\n" +
                        "    </fsu-firmendaten>\n" +
                        "</fsu-firmendatenexport>\n",
                "<fsu-firmendatenexport>\n" +
                        "    <fsu-firmendaten>\n" +
                        "        <crefonummer>4120035138</crefonummer>\n" +
                        "        <clz-eigner-vc>412</clz-eigner-vc>\n" +
                        "    </fsu-firmendaten>\n" +
                        "</fsu-firmendatenexport>\n",
        };

        checkreplacementForRestore(replacer, "4120035135.xml", xmlFragmentsArray[0], "1234567891", "123", reverseReplacementsEignerVC);
        checkreplacementForRestore(replacer, "4120035136.xml", xmlFragmentsArray[1], "1234567892", "456", reverseReplacementsEignerVC);
        checkreplacementForRestore(replacer, "4120035137.xml", xmlFragmentsArray[2], "1234567893", "789", reverseReplacementsEignerVC);
        checkreplacementForRestore(replacer, "4120035138.xml", xmlFragmentsArray[2], "1234567893", "987", reverseReplacementsEignerVC);
    }

    private void checkreplacementForRestore(Replacer replacer, String keyOriginalMap, String inString, String newCrefo, String eignerVC, Map<String, String> reverseReplacementsEignerVC) {
        StringBuilder replacementB = replacer.replace(keyOriginalMap, inString);
        String replacementX = replacementB.toString();
        Assert.assertTrue(replacementX.contains(newCrefo));
        Assert.assertTrue(replacementX.contains(eignerVC));
    }

}
