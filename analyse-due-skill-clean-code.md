# Clean Code Analyse — `testsupport_client` (View-Layer)

Analysiert mit dem Skill `clean-code` (DRY, KISS, YAGNI, Naming, Functions, Code Smells).
Analysierte Dateien: `TestSupportView.java`, `TestSupportMainTabView.java`, `TestSupportMainControlsView.java`

---

## Critical

### 1. DRY-Verletzung: Wiederholtes Thread-Boilerplate (5x)

Das Muster `disable → setWaitCursor → new Thread → try/catch/finally → enable → unsetCursor`
wiederholt sich in:

- `startActivitiProcess()` — Zeile 136
- `initForEnvironment()` — Zeile 273
- `doChangeTestResources()` — Zeile 474
- `doChangeITSQRevision()` — Zeile 492
- `startUserTaskRunnable()` — Zeile 418

```java
// ❌ 5x dasselbe Schema:
new Thread(() -> {
    SwingUtilities.invokeLater(() -> {
        GUIStaticUtils.setWaitCursor(this, true);
        enableComponentsToOnOff(false);
    });
    try { ... }
    catch (Exception ex) { ... }
    finally {
        SwingUtilities.invokeLater(() -> {
            GUIStaticUtils.setWaitCursor(this, false);
            enableComponentsToOnOff(true);
        });
    }
}).start();
```

**Refactoring:** Eine Hilfsmethode `runInWorkerThread(Runnable task, Consumer<Exception> onError)`
kapselt das Boilerplate und reduziert 5 Duplikate auf einen einzigen Aufruf.

---

### 2. DRY-Verletzung: Wiederholtes Exception-Wrapping (5x)

In `initHostsFields()`, `initTestSourcesComboBox()`, `initITSQRevisionsComboBox()`,
`initTestTypesComboBox()`, `doChangeComboBoxesHost()`:

```java
// ❌ 5x exakt dasselbe:
} catch (Exception ex) {
    GUIStaticUtils.showExceptionMessage(this, "Fehler beim ...", ex);
    throw new RuntimeException(ex.getMessage());
}
```

**Refactoring:** Hilfsmethode `showAndRethrow(String message, Exception ex)`.

---

## High

### 3. God Class: `TestSupportView` ist zu groß

Die Klasse übernimmt gleichzeitig zu viele Verantwortlichkeiten:

- GUI-Initialisierung (7 `init*`-Methoden)
- Activiti-Prozesssteuerung
- Umgebungswechsel + Lock-Management
- Kunden-Initialisierung
- Task-Variablen-Aufbau
- JVM-Verwaltung
- `TesunClientJobListener`-Protokoll (Dispatch + Darstellung)

**Kandidaten für Extraktion:**
- `EnvironmentSwitchHandler` — kapselt `doChangeEnvironment()`, `checkEnvironmentLock()`
- `CustomerInitializer` — kapselt `initCustomers()`, `initTestCasesForCustomers()`, `checkAndSetTestsSource()`
- Teile von `notifyClientJob()` in typisierte Handler

---

### 4. Lange Methode: `askClientJob()` (50+ Zeilen if-else-Kette)

```java
// ❌ 13 aufeinanderfolgende if-else auf einem Enum:
if (askFor.equals(ASK_FOR.ASK_OBJECT_RETRY)) { ... }
else if (askFor.equals(ASK_FOR.ASK_OBJECT_CONTINUE)) { ... }
else if (askFor.equals(ASK_FOR.ASK_OBJECT_CTE_VERSION)) { ... }
// ... 10 weitere
```

**Refactoring:** In Java 11: `switch`-Statement + private Methoden pro Fall
(`handleAskRetry()`, `handleAskContinue()` etc.).
In Java 14+: `switch`-Ausdruck.

---

### 5. Lange Methode: `notifyClientJob()` — `instanceof`-Kette statt Polymorphismus

```java
// ❌ Typ-Dispatch via instanceof:
if (notifyObject instanceof CteActivitiTask) { ... }
else if (notifyObject instanceof InputStream) { ... }
else if (notifyObject instanceof String) { ... }
else if (notifyObject == null) { ... }
else if (notifyObject instanceof Exception) { ... }
```

**Refactoring:** Typisierte Overloads in `TesunClientJobListener` oder ein
`NotifyEvent`-Interface mit konkreten Implementierungen (`TaskNotifyEvent`,
`ImageNotifyEvent`, `MessageNotifyEvent` etc.).

---

## Medium

### 6. Raw Threads ohne Namen

```java
// ❌ Anonyme Threads — schwer in Logs/Profiler zu identifizieren:
new Thread(() -> { ... }).start();

// ✅ Besser: benannte Threads oder ExecutorService:
Thread t = new Thread(() -> { ... }, "activiti-process-runner");
t.start();
```

---

### 7. Magic Numbers

```java
// ❌
getSplitPaneMain().setDividerLocation(500);   // Was bedeutet 500?
GUIStaticUtils.warteBisken(1000);             // 1 Sekunde? warum?
System.exit(-1);                              // Kein Kommentar, kein Constant

// ✅
private static final int MAIN_DIVIDER_POSITION    = 500;
private static final int JVM_DIALOG_OPEN_DELAY_MS = 1000;
private static final int EXIT_CODE_CONFIG_MISSING  = -1;
```

---

### 8. Typo im Methodennamen

```java
// ❌ TestSupportView.java, Zeile 255:
private static void checkEnvionmentLock(...)
//                        ↑ "Envioment" statt "Environment"

// ✅
private static void checkEnvironmentLock(...)
```

---

### 9. Raw Iterator statt For-Each

In `TestSupportView.initTestCasesForCustomers()` (Zeile 344) und
`TestSupportMainControlsView.initEnvironmentsComboBox()`:

```java
// ❌
Iterator<TEST_PHASE> iterator = customerTestInfoMapMap.keySet().iterator();
while (iterator.hasNext()) {
    TEST_PHASE testPhase = iterator.next();
    ...
}

// ✅
for (TEST_PHASE testPhase : customerTestInfoMapMap.keySet()) {
    ...
}
```

---

### 10. String-Konkatenation innerhalb `append()`

```java
// ❌ TestSupportView.java, Zeile 552 — unnötige Konkatenation inside append():
strErrBuilder.append("\n\t->" + errorTxtFile.getAbsolutePath());

// ✅ Chaining nutzen:
strErrBuilder.append("\n\t->").append(errorTxtFile.getAbsolutePath());
```

---

### 11. Schlechter Variablenname

```java
// ❌ TestSupportView.java, Zeile 367:
ManageJvmsDlg theView = new ManageJvmsDlgView(...);
// "theView" ist nichtssagend

// ✅
ManageJvmsDlg manageJvmsDialog = new ManageJvmsDlgView(...);
```

---

### 12. Raw Types in `TestSupportMainControlsView`

```java
// ❌ Zeilen 50, 61, 65 etc.:
DefaultComboBoxModel environmentsModel = new DefaultComboBoxModel();

// ✅
DefaultComboBoxModel<String> environmentsModel = new DefaultComboBoxModel<>();
```

---

## Minor / Style

### 13. Kommentare erklären "Was", nicht "Warum"

```java
// "Fix 1:", "Fix 2:", "Fix 3:" in enableComponentsToOnOff() —
// besser wäre eine kurze Begründung oder ein Issue-Referenz statt technischer Fix-Aufzählung
```

### 14. `/* CLAUDE_MODE */`-Blöcke — toter Code

Dauerhaft auskommentierter Code (mehrere `CLAUDE_MODE`-Blöcke in `TestSupportView`) ist
besser in der Git-History aufgehoben als im Quellcode. Falls diese Blöcke dauerhaft inaktiv
bleiben, sollten sie entfernt werden.

---

## Zusammenfassung

| Schweregrad | Anzahl | Beispiele |
|---|---|---|
| **Critical** (DRY) | 2 | Thread-Boilerplate, Exception-Wrapping |
| **High** (God Class, lange Methoden) | 3 | `TestSupportView`, `askClientJob()`, `notifyClientJob()` |
| **Medium** (Naming, Magic Numbers, Raw Types) | 7 | Typo, Raw Iterator, Magic Numbers, Raw Types |
| **Minor** | 2 | Kommentarstil, toter Code |

---

## Empfohlene Reihenfolge

### Quick Wins (< 1h, risikoarm)
1. Typo `checkEnvionmentLock` → `checkEnvironmentLock` korrigieren
2. Raw Iterator → For-Each umstellen (2 Stellen)
3. Magic Numbers als Konstanten extrahieren
4. Raw `DefaultComboBoxModel` generifizieren
5. `append("..." + var)` → `append("...").append(var)`
6. `theView` → `manageJvmsDialog` umbenennen

### Mittlere Refactorings (halber Tag)
7. Thread-Boilerplate in `runInWorkerThread()` zusammenführen
8. Exception-Wrapping in Hilfsmethode extrahieren
9. `askClientJob()` in switch + private Methoden aufteilen

### Größere Refactorings (nach Bedarf)
10. `notifyClientJob()` — `instanceof`-Kette durch typisierte Events ersetzen
11. `TestSupportView` aufteilen: `EnvironmentSwitchHandler`, `CustomerInitializer`
