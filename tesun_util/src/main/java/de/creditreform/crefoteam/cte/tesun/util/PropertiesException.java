package de.creditreform.crefoteam.cte.tesun.util;

public class PropertiesException extends Exception {
    /**
     * Use serialVersionUID for interoperability.
     */
    private final static long serialVersionUID = 2868255910742924901L;

    public PropertiesException(String exceptionMessage, Throwable throwable) {
        super(exceptionMessage, throwable);
    }

    public PropertiesException(String exceptionMessage) {
        this(exceptionMessage, null);
    }

}
