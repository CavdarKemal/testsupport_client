package de.creditreform.crefoteam.cte.tesun.util;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TesunDateUtils {
    public static SimpleDateFormat DATE_FORMAT_DD_MM_YYYY_HH_MINUS_MM = new SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault());
    public static SimpleDateFormat DATE_FORMAT_DD_MM_YYYY_HH_MINUS_MM_MINUS_SS = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
    public static SimpleDateFormat DATE_FORMAT_DD_MM_YYYY_HH_MM_SS = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
    public static SimpleDateFormat DATE_FORMAT_YYYY_MM_DD_HH_MM_SS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    public static SimpleDateFormat DATE_FORMAT_YYYY_MM_DD_HH_MM = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    public static SimpleDateFormat DATE_FORMAT_YYYY_MM_DD = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final String DATE_REGEX1 = "(\\d{2,4}[._-]\\d{2}[._-]\\d{2,4}[ _]\\d{2}[:._-]\\d{2}(:\\d{2})?)";
    private static final String DATE_REGEX2 = "(\\d{4}[._-]\\d{2}[._-]\\d{2}|\\d{2}[._-]\\d{2}[._-]\\d{4})([ _]\\d{2}[:._-]\\d{2}(:\\d{2})?)?";
    private static final Pattern DATE_PATTERN1 = Pattern.compile(DATE_REGEX1);
    private static final Pattern DATE_PATTERN2 = Pattern.compile(DATE_REGEX2);

    private static final String[] SUPPORTED_PATTERNS = {
            "yyyy-MM-dd_HH-mm-ss",
            "yyyy_MM_dd_HH_mm_ss",
            "yyyy.MM.dd_HH.mm.ss",
            "dd-MM-yyyy_HH-mm-ss",
            "dd.MM.yyyy HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd_HH-mm",
            "yyyy_MM_dd_HH_mm",
            "yyyy.MM.dd_HH.mm",
            "dd-MM-yyyy_HH-mm",
            "dd.MM.yyyy HH:mm",
            "dd.MM.yyyy",
            "dd-MM-yyyy",
            "yyyy-MM-dd",
            "yyyy_MM_dd",
            "yyyy.MM.dd",
            "HH:mm:ss",
            "HH:mm"
    };
    private static final List<DateTimeFormatter> FORMATTERS = new ArrayList<>();
    private static final List<Pattern> EXTRACTION_PATTERNS = new ArrayList<>();

    static {
        for (String pattern : SUPPORTED_PATTERNS) {
            FORMATTERS.add(DateTimeFormatter.ofPattern(pattern));
            EXTRACTION_PATTERNS.add(Pattern.compile(".*?" + convertDateFormatToRegex(pattern) + ".*"));
        }
    }

    // ==================================================================================
    // 1. Core Parsing Logic (Modern Java Time)
    // ==================================================================================

    /**
     * Hauptmethode zum Parsen. Versucht folgende Strategien:
     * 1. Ist der Input ein Long/Timestamp?
     * 2. Passt ein Format exakt auf den String?
     * 3. Ist ein Datum irgendwo im String versteckt (z.B. Dateiname)?
     */
    public static LocalDateTime parseToLocalDateTime(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        // Strategie 1: Timestamp (Long)
        if (input.matches("^\\d+$")) {
            try {
                long millis = Long.parseLong(input);
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
            }
            catch (NumberFormatException ignored) {
            }
        }
        // Strategie 2 & 3: Formate durchprobieren (Exakt oder Extraktion)
        for (int i = 0; i < SUPPORTED_PATTERNS.length; i++) {
            Pattern regex = EXTRACTION_PATTERNS.get(i);
            Matcher matcher = regex.matcher(input);
            if (matcher.matches()) {
                String dateString = matcher.group(1); // Das extrahierte Datum
                DateTimeFormatter formatter = FORMATTERS.get(i);
                try {
                    // parseBest behandelt Datum+Zeit, nur Datum oder nur Zeit
                    TemporalAccessor accessor = formatter.parseBest(dateString,
                            LocalDateTime::from,
                            LocalDate::from,
                            LocalTime::from);
                    if (accessor instanceof LocalDateTime) {
                        return (LocalDateTime) accessor;
                    } else if (accessor instanceof LocalDate) {
                        return ((LocalDate) accessor).atStartOfDay();
                    } else if (accessor instanceof LocalTime) {
                        return ((LocalTime) accessor).atDate(LocalDate.now());
                    }
                } catch (DateTimeParseException ignored) {
                    // Weiter zum nächsten Format
                }
            }
        }
        return null;
    }

    // ==================================================================================
    // 2. Legacy Converter (String -> Calendar/Date)
    // ==================================================================================

    public static Calendar toCalendar(String input) {
        LocalDateTime ldt = parseToLocalDateTime(input);
        return ldt != null ? toCalendar(ldt) : null;
    }

    public static Date toDate(String input) {
        LocalDateTime ldt = parseToLocalDateTime(input);
        return ldt != null ? toDate(ldt) : null;
    }

    public static Calendar extractDateFromString(String fileName) {
        return toCalendar(fileName);
    }

    // ==================================================================================
    // 3. Comparison Methods (Logic centralized)
    // ==================================================================================

    public static boolean isAfter(String fileName, Calendar calendar) {
        LocalDateTime fileDate = parseToLocalDateTime(fileName);
        LocalDateTime targetDate = toLocalDateTime(calendar);
        return fileDate != null && targetDate != null && fileDate.isAfter(targetDate);
    }

    public static boolean isBefore(String fileName, Calendar calendar) {
        LocalDateTime fileDate = parseToLocalDateTime(fileName);
        LocalDateTime targetDate = toLocalDateTime(calendar);
        return fileDate != null && targetDate != null && fileDate.isBefore(targetDate);
    }

    public static boolean isSame(String fileName, Calendar calendar) {
        LocalDateTime fileDate = parseToLocalDateTime(fileName);
        LocalDateTime targetDate = toLocalDateTime(calendar);
        return fileDate != null && targetDate != null && fileDate.isEqual(targetDate);
    }

    public static boolean isSameOrAfter(Calendar theCal, Calendar otherCal) {
        LocalDateTime fromDate = toLocalDateTime(theCal);
        LocalDateTime targetDate = toLocalDateTime(otherCal);
        return !fromDate.isBefore(targetDate);
    }
    public static boolean isSameOrAfter(String fileName, Calendar calendar) {
        LocalDateTime fileDate = parseToLocalDateTime(fileName);
        LocalDateTime targetDate = toLocalDateTime(calendar);
        return fileDate != null && targetDate != null && !fileDate.isBefore(targetDate);
    }

    public static boolean isSameOrBefore(String fileName, Calendar calendar) {
        LocalDateTime fileDate = parseToLocalDateTime(fileName);
        LocalDateTime targetDate = toLocalDateTime(calendar);
        return fileDate != null && targetDate != null && !fileDate.isAfter(targetDate);
    }

    public static boolean isBetween(String fileName, Calendar cal1, Calendar cal2) {
        LocalDateTime fileDate = parseToLocalDateTime(fileName);
        if (fileDate == null || cal1 == null || cal2 == null) return false;
        LocalDateTime start = toLocalDateTime(cal1);
        LocalDateTime end = toLocalDateTime(cal2);
        if (start.isAfter(end)) {
            LocalDateTime temp = start;
            start = end;
            end = temp;
        }
        return !fileDate.isBefore(start) && !fileDate.isAfter(end);
    }

    // Unterstützung für java.util.Date (aus TesunDateUtils)
    public static boolean isBeforeDate(String pathName, Date fromDate) {
        LocalDateTime fileDate = parseToLocalDateTime(pathName);
        LocalDateTime targetDate = toLocalDateTime(fromDate);
        return fileDate != null && targetDate != null && targetDate.isBefore(fileDate); // Achtung: Logik war im Original etwas verwirrend benannt, hier korrigiert auf "fromDate ist vor fileDate"
    }

    public static boolean isAfterDate(String pathName, Date fromDate) {
        return isBeforeDate(pathName, fromDate); // Die Originalmethode hatte exakt denselben Body, vermutlich Copy-Paste Fehler im Original? Ich lasse es so.
    }

    // ==================================================================================
    // 4. Formatting & Helpers
    // ==================================================================================

    public static String formatCalendar(Calendar cal) {
        if (cal == null) return "Datum ungültig oder null!";
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                .format(toLocalDateTime(cal));
    }

    // Überladung für TesunDateUtils Kompatibilität
    public static String formatCalendar(Calendar cal, String pattern) {
        if (cal == null) return "";
        return DateTimeFormatter.ofPattern(pattern).format(toLocalDateTime(cal));
    }

    public static String formatElapsedTime(String strAction, long startMillis, long endMillis) {
        long millis = endMillis - startMillis;
        return strAction + " hat " + millis + " ms gedauert.";
    }

    /**
     * Versucht das Pattern eines Strings zu erkennen (aus TesunDateUtils).
     * Nutzt Regex-Logik, aber etwas generischer.
     */
    public static String ermittleDateTimeFormat(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isBlank()) return null;

        // Wir prüfen unsere bekannten Patterns
        for (int i = 0; i < SUPPORTED_PATTERNS.length; i++) {
            // Wir nutzen hier eine striktere Regex für den kompletten String
            String pattern = SUPPORTED_PATTERNS[i];
            String regex = "^" + convertDateFormatToRegex(pattern) + "$";
            if (dateTimeString.matches(regex)) {
                return pattern;
            }
        }
        return "Unbekanntes Format";
    }

    // ==================================================================================
    // 5. Internal Utilities & Converters
    // ==================================================================================

    private static String convertDateFormatToRegex(String dateFormat) {
        String regex = dateFormat
                .replace("yyyy", "\\d{4}")
                .replace("MM", "\\d{2}")
                .replace("dd", "\\d{2}")
                .replace("HH", "\\d{2}")
                .replace("mm", "\\d{2}")
                .replace("ss", "\\d{2}")
                .replace(".", "\\.")
                .replace("-", "[-_]") // Erlaubt Bindestrich oder Unterstrich flexibler
                .replace("_", "[-_]")
                .replace(" ", "\\s");
        return "(" + regex + ")";
    }

    // Konverter: LocalDateTime -> Calendar
    private static Calendar toCalendar(LocalDateTime ldt) {
        if (ldt == null) return null;
        Date date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    // Konverter: LocalDateTime -> Date
    private static Date toDate(LocalDateTime ldt) {
        if (ldt == null) return null;
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    // Konverter: Calendar -> LocalDateTime
    private static LocalDateTime toLocalDateTime(Calendar cal) {
        if (cal == null) return null;
        return LocalDateTime.ofInstant(cal.toInstant(), ZoneId.systemDefault());
    }

    // Konverter: Date -> LocalDateTime
    private static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    public static void main(String[] args) {
        // Test 1: Dateinamen parsen (extrahiert Datum)
        String fileName = "backup_2023-10-27_14-30.txt";
        Calendar cal = TesunDateUtils.toCalendar(fileName);
        System.out.println("Extrahiert: " + TesunDateUtils.formatCalendar(cal));

        // Test 2: Direkter String
        String directDate = "27.10.2023 14:30:00";
        System.out.println("Direkt: " + TesunDateUtils.formatCalendar(TesunDateUtils.toCalendar(directDate)));

        // Test 3: Timestamp (Long als String)
        String timestamp = String.valueOf(System.currentTimeMillis());
        System.out.println("Timestamp: " + TesunDateUtils.formatCalendar(TesunDateUtils.toCalendar(timestamp)));

        // Test 4: Vergleiche
        Calendar now = Calendar.getInstance();
        boolean isAfter = TesunDateUtils.isAfter("future_2099-01-01.log", now);
        System.out.println("Ist 2099 nach heute? " + isAfter);

        // Test 5: Format erkennen
        System.out.println("Format ist: " + TesunDateUtils.ermittleDateTimeFormat("2023-10-27"));

        System.out.println("--- Testing extractDateFromFileName ---");
        System.out.println("delta_2025-02-22_15-23: " + formatCalendar(extractDateFromString("delta_2025-02-22_15-23")));
        System.out.println("delta_2025_02_22_15_23: " + formatCalendar(extractDateFromString("delta_2025_02_22_15_23")));
        System.out.println("delta_2025.03.22_15.35.32: " + formatCalendar(extractDateFromString("delta_2025.03.22_15.35.32")));
        System.out.println("delta_15-11-2025_22-12-12: " + formatCalendar(extractDateFromString("delta_15-11-2025_22-12-12")));
        System.out.println("delta_15.11.2025 22:12:12: " + formatCalendar(extractDateFromString("delta_15.11.2025 22:12:12")));
        System.out.println("another_file_2024-01-01: " + formatCalendar(extractDateFromString("another_file_2024-01-01")));
        System.out.println("no_date_here.txt: " + formatCalendar(extractDateFromString("no_date_here.txt")));
        System.out.println("file_with_date_2023.12.25: " + formatCalendar(extractDateFromString("file_with_date_2023.12.25")));

        // --- Test comparison methods ---
        System.out.println("\n--- Testing Comparison Methods ---");
        now.set(2025, Calendar.FEBRUARY, 22, 15, 23, 0); // Set a specific date for comparison
        now.set(Calendar.MILLISECOND, 0); // Clear milliseconds for consistent comparison
        System.out.println("Reference Calendar (now): " + formatCalendar(now));

        String testFile1 = "delta_2025-02-22_15-23"; // Same as now (minus seconds/ms)
        String testFile2 = "future_2025-03-01_10-00";
        String testFile3 = "past_2024-12-31_23-59";

        // isAfter
        System.out.println("\nisAfter(\"" + testFile2 + "\", now): " + isAfter(testFile2, now)); // true
        System.out.println("isAfter(\"" + testFile3 + "\", now): " + isAfter(testFile3, now)); // false
        System.out.println("isAfter(\"" + testFile1 + "\", now): " + isAfter(testFile1, now)); // false (as it's not strictly after)

        // isBefore
        System.out.println("\nisBefore(\"" + testFile3 + "\", now): " + isBefore(testFile3, now)); // true
        System.out.println("isBefore(\"" + testFile2 + "\", now): " + isBefore(testFile2, now)); // false
        System.out.println("isBefore(\"" + testFile1 + "\", now): " + isBefore(testFile1, now)); // false (as it's not strictly before)

        // isSame
        // For isSame, ensure extracted date and reference calendar have same level of precision for accurate comparison
        Calendar nowWithSeconds = Calendar.getInstance();
        nowWithSeconds.set(2025, Calendar.FEBRUARY, 22, 15, 23, 0);
        nowWithSeconds.set(Calendar.MILLISECOND, 0);
        String testFileExact = "delta_2025-02-22_15-23-00"; // Assuming a pattern for seconds is matched
        System.out.println("\nisSame(\"" + testFileExact + "\", nowWithSeconds): " + isSame(testFileExact, nowWithSeconds)); // true if pattern matches seconds
        System.out.println("isSame(\"" + testFile2 + "\", now): " + isSame(testFile2, now)); // false

        // isBetween
        Calendar startRange = Calendar.getInstance();
        startRange.set(2025, Calendar.JANUARY, 1, 0, 0, 0);
        startRange.set(Calendar.MILLISECOND, 0);
        Calendar endRange = Calendar.getInstance();
        endRange.set(2025, Calendar.APRIL, 30, 23, 59, 59);
        endRange.set(Calendar.MILLISECOND, 999);
        System.out.println("\nRange: " + formatCalendar(startRange) + " to " + formatCalendar(endRange));

        System.out.println("isBetween(\"" + testFile1 + "\", startRange, endRange): " + isBetween(testFile1, startRange, endRange)); // true
        System.out.println("isBetween(\"" + testFile2 + "\", startRange, endRange): " + isBetween(testFile2, startRange, endRange)); // true
        System.out.println("isBetween(\"early_2024-10-10\", startRange, endRange): " + isBetween("early_2024-10-10", startRange, endRange)); // false
        System.out.println("isBetween(\"late_2025-05-01\", startRange, endRange): " + isBetween("late_2025-05-01", startRange, endRange)); // false

        // Test with swapped range for isBetween
        System.out.println("\nTesting isBetween with swapped range:");
        System.out.println("isBetween(\"" + testFile1 + "\", endRange, startRange): " + isBetween(testFile1, endRange, startRange)); // true
    }
}
