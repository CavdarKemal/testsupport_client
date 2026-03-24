package de.creditreform.crefoteam.cte.tesun.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.List;

public class DateConverter {

    private static final List<DateTimeFormatter> FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"),
            DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd_HH.mm.ss"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"),
            DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd_HH.mm"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy_MM_dd"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd"),
            DateTimeFormatter.ofPattern("HH:mm:ss")
    );

    /**
     * Versucht, den Eingabestring in ein LocalDateTime umzuwandeln.
     * <p>
     * Logik:
     * 1. Wenn Datum & Zeit vorhanden sind -> LocalDateTime
     * 2. Wenn nur Datum vorhanden ist -> Start des Tages (00:00 Uhr)
     * 3. Wenn nur Zeit vorhanden ist -> Heutiges Datum + Zeit
     *
     * @param dateString Das Datum als String
     * @return LocalDateTime Objekt
     * @throws IllegalArgumentException wenn keines der Formate passt.
     */
    public static LocalDateTime parse(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            throw new IllegalArgumentException("Input string must not be null or empty");
        }
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                // parseBest versucht, so viele Informationen wie möglich zu extrahieren
                TemporalAccessor accessor = formatter.parseBest(dateString, LocalDateTime::from, LocalDate::from, LocalTime::from);
                if (accessor instanceof LocalDateTime) {
                    return (LocalDateTime) accessor;
                } else if (accessor instanceof LocalDate) {
                    return ((LocalDate) accessor).atStartOfDay();
                } else if (accessor instanceof LocalTime) {
                    return ((LocalTime) accessor).atDate(LocalDate.now());
                }
            } catch (DateTimeParseException ignored) {
                // Dieses Format hat nicht gepasst, wir probieren das nächste
            }
        }
        throw new IllegalArgumentException("Kein unterstütztes Datumsformat gefunden für: " + dateString);
    }

    public static String format(LocalDateTime dateTime, String pattern) {
        return DateTimeFormatter.ofPattern(pattern).format(dateTime);
    }

    public static void main(String[] args) {
        String[] testInputs = {
                "2025-02-13_12-22-34",
                "2025_02_13_12_22_34",
                "2025.02.13_12.22.34",
                "13-02-2025_12-22-34",
                "13.02.2025 12:22:34",
                "2025-02-13_12-22",
                "2025_02_13_12_22",
                "2025.02.13_12.22",
                "13-02-2025_12-22",
                "13.02.2025 12:22",
                "13.02.2025",
                "13-02-2025",
                "2025-02-13",
                "2025_02_13",
                "2025.02.13",
                "12:22:34",
        };
        for (String input : testInputs) {
            try {
                LocalDateTime result = DateConverter.parse(input);
                System.out.println("Input: '" + input + "' -> Output: " + result);
            } catch (IllegalArgumentException e) {
                System.err.println("Fehler: " + e.getMessage());
            }
        }
    }
}