package de.creditreform.crefoteam.cte.tesun.util.propertyfiles;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import de.creditreform.crefoteam.cte.tesun.util.NameCrefo;
import de.creditreform.crefoteam.cte.tesun.util.TestCrefo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

/**
 * Ableitung von {@link PropertyFileLoaderAdapter} für die Block-weise
 * Verarbeitung der Paare aus Testfall-Bezeichnung und Crefonummer
 * User: ralf
 * Date: 05.05.14
 * Time: 14:53
 */
public class PropertyFileLoaderAdapterBulk extends PropertyFileLoaderAdapter<PropertyFileLoaderBulkFunction> {

    private final SetMultimap<PathInfo, NameCrefo> collectedEntries;

    public PropertyFileLoaderAdapterBulk(PropertyFileLoaderBulkFunction function) {
        super(function);
        this.collectedEntries = HashMultimap.create();
    }

    @Override
    public void reset() {
        collectedEntries.clear();
    }

    @Override
    public void collectOrProcess(PathInfo baseOutputPath, String testFallName, Long crefonummer) throws Exception {
        collectedEntries.put(baseOutputPath, new NameCrefo(testFallName, crefonummer));
    }

    @Override
    public void processCollected() throws Exception {
        TreeMap<PathInfo, NameCrefo> treeMap = new TreeMap(collectedEntries.asMap());
        // Die Crefos aus verschiedenen Basis-Verzeichnissen werden getrennt übergeben
        for (PathInfo baseDirKey : treeMap.keySet()) {
            Collection<NameCrefo> crefoSet = (Collection<NameCrefo>) treeMap.get(baseDirKey);
            List<TestCrefo> testCrefosList = convertToTestCrefos(crefoSet);
            getFunction().applyBulk(baseDirKey, testCrefosList);
        }
    }

    private static List<TestCrefo> convertToTestCrefos(Collection<NameCrefo> crefoSet) {
        List<TestCrefo> testCrefosList = new ArrayList<>();
        crefoSet.stream().forEach(nameCrefo -> {
            TestCrefo testCrefo = new TestCrefo(nameCrefo.getTestFallName(), nameCrefo.getTestFallCrefo(), "", false, null);
            testCrefosList.add(testCrefo);
        });
        return testCrefosList;
    }
}
