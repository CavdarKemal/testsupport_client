package de.creditreform.crefoteam.cte.tesun.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InstrumentationExample {
    public static void printObjectSize(Object object) {
        System.out.println("Object type: " + object.getClass() +
                ", size: " + InstrumentationAgent.getObjectSize(object) + " bytes");
    }

    public static void main(String[] arguments) {
        String emptyString = "";
        String string = "Estimating Object Size Using Instrumentation";
        String[] stringArray = {emptyString, string, "com.baeldung"};
        String[] anotherStringArray = new String[100];
        List<String> emptyStringList = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder(100);
        int maxIntPrimitive = Integer.MAX_VALUE;
        int minIntPrimitive = Integer.MIN_VALUE;
        Integer maxInteger = Integer.MAX_VALUE;
        Integer minInteger = Integer.MIN_VALUE;
        long zeroLong = 0L;
        double zeroDouble = 0.0;
        boolean falseBoolean = false;
        Object object = new Object();

        class EmptyClass {
        }
        EmptyClass emptyClass = new EmptyClass();

        class StringClass {
            public String s;
        }
        StringClass stringClass = new StringClass();

        List<String> stringList = Arrays.asList("printObjectSize(emptyString);printObjectSize(emptyString);printObjectSize(emptyString);printObjectSize(emptyString);printObjectSize(emptyString);printObjectSize(emptyString);",
                "printObjectSize(stringArray);printObjectSize(stringArray);printObjectSize(stringArray);printObjectSize(stringArray);printObjectSize(stringArray);printObjectSize(stringArray);printObjectSize(stringArray);printObjectSize(stringArray);printObjectSize(stringArray);",
                "printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);printObjectSize(Day.TUESDAY);");

        printObjectSize(emptyString);
        printObjectSize(string);
        printObjectSize(stringArray);
        printObjectSize(anotherStringArray);
        printObjectSize(emptyStringList);
        printObjectSize(stringBuilder);
        printObjectSize(maxIntPrimitive);
        printObjectSize(minIntPrimitive);
        printObjectSize(maxInteger);
        printObjectSize(minInteger);
        printObjectSize(zeroLong);
        printObjectSize(zeroDouble);
        printObjectSize(falseBoolean);
        printObjectSize(Day.TUESDAY);
        printObjectSize(object);
        printObjectSize(emptyClass);
        printObjectSize(stringClass);
        printObjectSize(stringList);
    }

    public enum Day {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }
}
