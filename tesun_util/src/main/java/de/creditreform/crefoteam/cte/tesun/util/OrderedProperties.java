package de.creditreform.crefoteam.cte.tesun.util;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Ableitung der Klasse {@link Properties}, die die Reihenfolge des Einfügens von Properties speichert. Auf
 * diese Weise können Properties in der Reihenfolge ihrer Nennung in einer Datei abgerufen werden. Zusätzlich
 * sind die load- und store-Methoden überschrieben und unterstützen eine automatische Umsetzung des Charset. Im
 * Gegensatz zu der Basis-Klasse ist der Default hier 'UTF-8'.
 * <p>
 * User: ralf
 * Date: 17.07.13
 * Time: 11:00
 */
public class OrderedProperties
        extends Properties {

    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * Adapter zwischen einem {@link Iterator} (ab JDK1.2) und einer {@link Enumeration} (ab JDK1.0)
     *
     * @param <T> Typ der Elemente
     */
    protected static class EnumerationBackedByIterator<T>
            implements Enumeration<T> {
        private final Iterator<T> iterator;

        public EnumerationBackedByIterator(Iterator<T> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasMoreElements() {
            return iterator.hasNext();
        }

        @Override
        public T nextElement() {
            return iterator.next();
        }
    }

    private final String loadStoreCharsetName;
    private transient Charset loadStoreCharset;
    private final ArrayList<Object> propertyNames;

    /**
     * Default-Konstruktor mit dem Default-Charset (UTF-8)
     */
    public OrderedProperties() {
        this(DEFAULT_CHARSET);
    }

    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        this.loadStoreCharset = (loadStoreCharsetName == null) ? null : Charset.forName(loadStoreCharsetName);
    }

    /**
     * Erweiterter Konstruktor mit wählbarem Charset. Null ist gleichbedeutend mit der Abschaltung der automatischen
     * Umsetzung und entspricht damit dem Verhalten der Basis-Klasse.
     *
     * @param loadStoreCharset ausgewähltes Charset für load und store
     */
    public OrderedProperties(Charset loadStoreCharset) {
        super();
        this.loadStoreCharset = loadStoreCharset;
        this.loadStoreCharsetName = loadStoreCharset == null ? null : loadStoreCharset.name();
        propertyNames = new ArrayList<>();
    }

    @Override
    public synchronized Enumeration<Object> propertyNames() {
        return new EnumerationBackedByIterator<>(propertyNames.iterator());
    }

    @Override
    public Set<String> stringPropertyNames() {
        Map<String, String> target = new LinkedHashMap<>();
        enumerateStringProperties(target);
        return target.keySet();
    }

    /**
     * Enumerates all key/value pairs in the specified hashtable
     * and omits the property if the key or value is not a string.
     *
     * @param target the hashtable
     */
    public synchronized void enumerateStringProperties(Map<String, String> target) {
        if (defaults != null) {
            enumerateStringProperties(defaults, target);
        }
        enumerateStringProperties(this, target);
    }

    protected void enumerateStringProperties(Properties p, Map<String, String> target) {
        for (Enumeration e = p.propertyNames(); e.hasMoreElements(); ) {
            Object k = e.nextElement();
            Object v = get(k);
            if (k instanceof String && v instanceof String) {
                target.put((String) k, (String) v);
            }
        }
    }

    @Override
    public synchronized Object put(Object key, Object value) {
        propertyNames.remove(key);
        propertyNames.add(key);
        return super.put(key, value);
    }

    @Override
    public synchronized Object remove(Object key) {
        propertyNames.remove(key);
        return super.remove(key);
    }

    /**
     * Convenience-Methode für das Übertragen aller String-Properties in eine Map
     *
     * @param targetMap Ziel für das Schreiben aller Properties
     */
    public void dumpPropertiesInto(Map<? super String, ? super String> targetMap) {
        for (String key : stringPropertyNames()) {
            targetMap.put(key, getProperty(key));
        }
    }

    @Override
    public synchronized void load(InputStream inStream)
            throws IOException {
        if (loadStoreCharset == null) {
            super.load(inStream);
        } else {
            // Laden mit automatischer Anpassung auf das vorgegebene Charset
            final InputStreamReader inputStreamReader = new InputStreamReader(inStream, loadStoreCharset);
            super.load(inputStreamReader);
        }
    }

    @Override
    public synchronized void loadFromXML(InputStream in)
            throws IOException {
        if (loadStoreCharset == null) {
            super.loadFromXML(in);
        } else {
            throw new UnsupportedOperationException("loadFromXML mit automatischer Umsetzung des Charset wird nicht unterstützt");
        }
    }

    @Override
    public void store(OutputStream out, String comments)
            throws IOException {
        if (loadStoreCharset == null) {
            super.store(out, comments);
        } else {
            // Speichern mit automatischer Anpassung auf das vorgegebene Charset
            final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(out, loadStoreCharset);
            super.store(outputStreamWriter, comments);
        }
    }

    @Override
    public void storeToXML(OutputStream os, String comment)
            throws IOException {
        if (loadStoreCharset == null) {
            super.storeToXML(os, comment);
        } else {
            super.storeToXML(os, comment, loadStoreCharset.name());
        }
    }

}
