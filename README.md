### testsupport_client

für die Fachwert-Analyse benötigten DB-Zugangsdaten:

---

## TestSupportViewActivitiTest — Session-Wissen

### Wie man den Test ausführt

`cit.cmd` ignoriert alle zusätzlichen Maven-Parameter (es führt immer fest `clean install` aus).
Daher den Test **direkt mit `mvn`** aus dem Modulverzeichnis starten:

```cmd
cd E:\Projekte\ClaudeCode\testsupport_client\TestSupportGUI
# JAVA_HOME muss auf JDK 11 zeigen (JAVA11_HOME)
set JAVA_HOME=%JAVA11_HOME%
set PATH=%JAVA_HOME%\bin;%MAVEN3912_HOME%\bin;%PATH%
mvn test
```

Oder aus der Git-Bash:
```bash
cd "E:/Projekte/ClaudeCode/testsupport_client/TestSupportGUI"
JAVA_HOME="/c/Program Files/Java/jdk-11.0.14.1+1" \
  PATH="/c/Program Files/Java/jdk-11.0.14.1+1/bin:/c/Program Files/ApacheMaven/apache-maven-3.9.12/bin:$PATH" \
  mvn test
```

**Hinweis `-Dtest=...` mit Surefire 2.12:** Das Flag funktioniert nicht zuverlässig (findet 0 Tests).
Alle Tests im Modul starten und das Ergebnis von `TestSupportViewActivitiTest` in der Ausgabe ablesen.

### Voraussetzungen

| Voraussetzung | Details |
|---|---|
| Activiti Docker | läuft auf `http://localhost:9090` |
| Credentials | `CAVDARK-ENE` / `cavdark` (in `ENE-config.properties`) |
| Properties-Datei | `src/test/resources/ENE-config.properties` |
| BPMN-Dateien | `src/test/resources/bpmns/CteAutomatedTestProcess.bpmn` |
| X-TESTS-Verzeichnis | `E:\Projekte\ClaudeCode\testsupport_client\X-TESTS` |

### Bekannte Probleme & Fixes

#### Problem 1: Alle Tests SKIPPED (häufigste Ursache)

**Symptom:** `Tests run: 3, Failures: 0, Errors: 0, Skipped: 3`

**Ursache:** `setUpClass()` schlägt still fehl (Exception wird gecatcht → `activitiService = null`).
Häufigste konkrete Ursache: Es gibt noch laufende Prozess-Instanzen auf Activiti aus einem
vorherigen Test-Run. Wenn `uploadActivitiProcessesFromClassPath` das alte Deployment löschen
will, verweigert PostgreSQL das mit einem Foreign-Key-Fehler (`act_fk_exe_procdef`).

**Fix:** In `setUpClass()` werden jetzt alle laufenden Prozesse gelöscht **bevor** das Deployment
ersetzt wird (siehe `TestSupportViewActivitiTest.java` Z. 65–67).

**Debugging:** Temporär `e.printStackTrace()` in den catch-Block einbauen, dann läuft der Test
nochmal und die Exception erscheint in der Maven-Konsole.

#### Problem 2: BUILD FAILURE beim `clean`-Schritt

**Symptom:** `Failed to delete TestSupportViewActivitiTest.txt`

**Ursache:** Ein vorheriger Surefire-Test-JVM (mit GUI) läuft noch und hält die Datei gesperrt.

**Fix:**
```bash
tasklist | grep java   # PIDs der Java-Prozesse anzeigen
taskkill //PID <pid> //F
```

#### Problem 4: test2 und test3 FAILURE — "Stop-Button nicht rechtzeitig aktiviert"

**Symptom:** `test2_laufendenProzessFortsetzten` und `test3_altenProzessBeendenUndNeuStarten` schlagen
fehl: `Stop-Button wurde nicht rechtzeitig aktiviert (Timeout: 120000 ms)`.

**Ursache:** `GUIStaticUtils.showConfirmDialog()` verwendet `VERTICAL_SCROLLBAR_ALWAYS` für den
JScrollPane im Dialog. Das fügt `BasicArrowButton`-Instanzen (Scrollbar-Pfeile) in die
Komponenten-Hierarchie ein, **bevor** die eigentlichen Yes/No/Cancel-Buttons erscheinen.
Jemmys `new JButtonOperator(dialog, 0)` findet daher den Scrollbar-Pfeil statt „Ja" →
Dialog wird nicht geschlossen → `startActivitiProcess()` blockiert → Stop-Button bleibt
disabled → `waitForStopButtonEnabled` läuft in Timeout.

**Fix:** Statt Index-basierter Button-Suche (`new JButtonOperator(dialog, 0)`) wird
Text-basierte Suche verwendet (`new JButtonOperator(dialog, "Ja")` / `"Nein"`).
Auf deutschem Windows liefert UIManager die deutschen Button-Texte (Ja/Nein/Abbrechen).

#### Problem 3: Tests laufen gar nicht (0 Tests found) wenn vom Root gebaut

**Symptom:** Maven baut von `testsupport_client/` Root → `tesun_util`-Tests schlagen fehl →
`TestSupportGUI` wird SKIPPED.

**Fix:** Direkt aus `TestSupportGUI/`-Verzeichnis bauen (siehe oben).

### Architektur der Tests

```
TestSupportViewActivitiTest (extends BaseGUITest)
  @BeforeClass setUpClass()
    → EnvironmentConfig("ENE")        -- liest ENE-config.properties aus Classpath
    → CteActivitiServiceRestImpl      -- REST-Client für Activiti
    → deleteProcessInstances()         -- laufende Prozesse löschen (FK-Constraint!)
    → uploadActivitiProcessesFromClassPath()  -- BPMN deployen
  @Before setUp()
    → Assume.assumeNotNull(activitiService, ENV_CONFIG)  -- skippt wenn Setup fehlschlug
    → deleteAllRunningProcesses()      -- sauberer Zustand vor jedem Test
    → new TestSupportGUI(ENV_CONFIG)   -- frisches GUI-Frame
    → waitForStartButtonEnabled(30s)
  Tests:
    test1: Frisch-Start → Stop-Button wird aktiviert
    test2: Prozess fortsetzen (Ja-Dialog) → selbe Instanz bleibt
    test3: Neu starten (Nein-Dialog) → alte Instanz weg, neue da
  @After tearDown()
    → deleteAllRunningProcesses()
    → guiFrame.dispose()
```

### BPMN-Deployment-Details

- BPMN-Dateien liegen in `src/test/resources/bpmns/`
- `%ENV%` im BPMN-Content wird durch `ENE` ersetzt
- Deployed als `ENE-CteAutomatedTestProcess.bpmn`
- Prozess-Key im Test: `ENE-TestAutomationProcess`
- Activiti-REST-Endpoint: `http://localhost:9090/activiti-rest/service/`

