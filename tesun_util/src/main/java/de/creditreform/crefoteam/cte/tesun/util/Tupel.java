package de.creditreform.crefoteam.cte.tesun.util;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * 2-elementiges Tupel bzw. Pair
 * User: ralf
 * Date: 19.02.14
 * Time: 12:43
 */
public class Tupel<T1, T2>
        implements Serializable, Map.Entry<T1, T2> {
    private final T1 element1;
    private T2 element2;

    public Tupel(T1 element1, T2 element2) {
        this.element1 = element1;
        this.element2 = element2;
    }

    public T1 getElement1() {
        return element1;
    }

    public T2 getElement2() {
        return element2;
    }

    @Override
    public T1 getKey() {
        return getElement1();
    }

    @Override
    public T2 getValue() {
        return getElement2();
    }

    @Override
    public T2 setValue(T2 value) {
        T2 oldValue = element2;
        element2 = value;
        return oldValue;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tupel)) return false;

        Tupel tupel = (Tupel) o;

        if (!Objects.equals(element1, tupel.element1)) return false;
        if (!Objects.equals(element2, tupel.element2)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = element1 != null ? element1.hashCode() : 0;
        result = 31 * result + (element2 != null ? element2.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return Tupel.class.getSimpleName() + "[" + element1 + "|" + element2 + "]";
    }

}
