package com.smartassignment.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Utility class for date/time calculations, formatting, and parsing.
 * Aligns with timezone requirements (default is Asia/Kolkata).
 */
public class DateUtil {
    private static final String DEFAULT_TIMEZONE = "Asia/Kolkata";
    private static final String HTML_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm";
    private static final String DISPLAY_FORMAT = "dd MMM yyyy, hh:mm a";

    /**
     * Parses a string from HTML5 datetime-local inputs into a java.sql.Timestamp.
     * 
     * @param dateTimeStr input datetime string from forms.
     * @return the Timestamp object, or null if parsing fails.
     */
    public static Timestamp parseHtmlDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(HTML_DATETIME_FORMAT);
            sdf.setTimeZone(TimeZone.getTimeZone(DEFAULT_TIMEZONE));
            Date date = sdf.parse(dateTimeStr);
            return new Timestamp(date.getTime());
        } catch (ParseException e) {
            // Fallback for full timestamp with seconds yyyy-MM-dd'T'HH:mm:ss
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone(DEFAULT_TIMEZONE));
                Date date = sdf.parse(dateTimeStr);
                return new Timestamp(date.getTime());
            } catch (ParseException ex) {
                return null;
            }
        }
    }

    /**
     * Formats a Timestamp into a human-readable display string (e.g. 18 Jun 2026, 04:30 PM).
     * 
     * @param ts the Timestamp.
     * @return formatted string representation.
     */
    public static String formatTimestampForDisplay(Timestamp ts) {
        if (ts == null) {
            return "-";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DISPLAY_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone(DEFAULT_TIMEZONE));
        return sdf.format(ts);
    }

    /**
     * Formats a Timestamp to be populated inside a datetime-local input field.
     * 
     * @param ts the Timestamp.
     * @return formatted HTML-ready datetime string.
     */
    public static String formatTimestampForHtmlInput(Timestamp ts) {
        if (ts == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(HTML_DATETIME_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone(DEFAULT_TIMEZONE));
        return sdf.format(ts);
    }

    /**
     * Compares the current system time to a given deadline.
     * 
     * @param deadline the deadline Timestamp.
     * @return true if the deadline has passed, false otherwise.
     */
    public static boolean isPastDeadline(Timestamp deadline) {
        if (deadline == null) {
            return false;
        }
        return new Date().after(deadline);
    }

    /**
     * Calculates the difference in minutes between two timestamps.
     * Useful for checking submission re-upload windows.
     * 
     * @param start the starting Timestamp.
     * @param end the ending Timestamp.
     * @return difference in minutes.
     */
    public static long getMinutesDifference(Timestamp start, Timestamp end) {
        if (start == null || end == null) {
            return 0;
        }
        long diffMs = end.getTime() - start.getTime();
        return diffMs / (1000 * 60);
    }
}
