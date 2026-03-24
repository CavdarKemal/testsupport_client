package de.creditreform.crefoteam.cte.tesun.util;

/**
 * Exception für die Signalisierung von Konfigurations-Fehlern im
 * Rahmen der Initialisierung der Anwendung
 * User: ralf
 * Date: 25.02.14
 * Time: 10:27
 */
public class TestSupportConfigurationException extends RuntimeException {
    public TestSupportConfigurationException(String message) {
        super(message);
    }

    public TestSupportConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
