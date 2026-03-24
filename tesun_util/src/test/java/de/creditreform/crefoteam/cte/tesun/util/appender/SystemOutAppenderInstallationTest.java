package de.creditreform.crefoteam.cte.tesun.util.appender;

import de.creditreform.crefoteam.technischebasis.log4j.SystemOutAppender;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Test-Klasse für {@link SystemOutAppender}
 * User: ralf
 * Date: 13.06.14
 * Time: 15:09
 */
public class SystemOutAppenderInstallationTest {
    private List<SystemOutAppender> collectSystemOutAppenders() {
        List<SystemOutAppender> collected = new ArrayList<>();
        Enumeration enumeration = Logger.getRootLogger().getAllAppenders();
        while (enumeration.hasMoreElements()) {
            Object app = enumeration.nextElement();
            if (app instanceof SystemOutAppender) {
                collected.add((SystemOutAppender) app);
            }
        }
        return collected;
    }

    private SystemOutAppender appInfo;
    private SystemOutAppender appWarn;

    @Before
    public void setUp() {
        appInfo = SystemOutAppender.INFO();
        appWarn = SystemOutAppender.WARN();
        Logger.getRootLogger().removeAllAppenders();
    }

    private void installConflictingAppender(SystemOutAppender newAppender) {
        SystemOutAppender installed2 = newAppender.installIntoRootLogger();

        List<SystemOutAppender> collected = collectSystemOutAppenders();
        Assert.assertEquals("Anzahl der SystemOutAppender sollte nie grösser als 1 sein", 1, collected.size());
        Assert.assertSame("Verbliebener Appender sollte derjenige mit Level.INFO sein", appInfo, collected.get(0));
        Assert.assertSame("Installation sollte den Appender mit Level.INFO zurückmelden", appInfo, installed2);
    }

    @Test
    public void testStandardInstallationConflict() {

        SystemOutAppender installed1 = appInfo.installIntoRootLogger();
        Assert.assertSame("Nach der Installation des 1. Appenders sollte dieser auch zurückgeliefert werden", appInfo, installed1);

        installConflictingAppender(appWarn);
        // Auch ein zweiter Appender mit gleichem Level sollte die
        // bestehende Instanz nicht ersetzen
        installConflictingAppender(SystemOutAppender.INFO());

    }

    @Test
    public void testManualInstallationConflict() {
        Logger.getRootLogger().addAppender(appInfo);

        installConflictingAppender(appWarn);

    }
}
