package com.rippedrss.android.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Thread-safe date parser for RSS/Atom feed dates.
 * Uses ThreadLocal to ensure SimpleDateFormat instances are not shared across threads.
 */
object ThreadSafeDateParser {

    // Date format patterns commonly used in RSS and Atom feeds
    private val DATE_PATTERNS = listOf(
        "EEE, dd MMM yyyy HH:mm:ss Z",      // RFC 822 (RSS standard)
        "EEE, dd MMM yyyy HH:mm:ss z",      // RFC 822 with timezone name
        "EEE, dd MMM yyyy HH:mm:ss",        // RFC 822 without timezone
        "yyyy-MM-dd'T'HH:mm:ssZ",           // ISO 8601 with timezone
        "yyyy-MM-dd'T'HH:mm:ssXXX",         // ISO 8601 with colon in timezone
        "yyyy-MM-dd'T'HH:mm:ss'Z'",         // ISO 8601 UTC
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",     // ISO 8601 with milliseconds
        "yyyy-MM-dd'T'HH:mm:ss.SSSZ",       // ISO 8601 with millis and timezone
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",     // ISO 8601 with millis and colon timezone
        "yyyy-MM-dd HH:mm:ss",              // Simple format
        "yyyy-MM-dd",                        // Date only
        "dd MMM yyyy HH:mm:ss Z",           // Without day name
        "dd MMM yyyy HH:mm:ss",             // Without day name and timezone
        "MM/dd/yyyy HH:mm:ss",              // US format
        "dd/MM/yyyy HH:mm:ss"               // European format
    )

    // ThreadLocal formatters for each pattern
    private val formatters: List<ThreadLocal<SimpleDateFormat>> = DATE_PATTERNS.map { pattern ->
        ThreadLocal.withInitial {
            SimpleDateFormat(pattern, Locale.US).apply {
                isLenient = false
            }
        }
    }

    /**
     * Parses a date string using multiple format patterns.
     *
     * @param dateString The date string to parse
     * @return The parsed timestamp in milliseconds, or current time if parsing fails
     */
    fun parse(dateString: String?): Long {
        if (dateString.isNullOrBlank()) {
            return System.currentTimeMillis()
        }

        val trimmed = dateString.trim()

        for (formatterHolder in formatters) {
            try {
                val formatter = formatterHolder.get()
                val date = formatter.parse(trimmed)
                if (date != null) {
                    return date.time
                }
            } catch (e: ParseException) {
                // Try next format
            }
        }

        // If no format worked, return current time
        return System.currentTimeMillis()
    }

    /**
     * Formats a timestamp to a standard RFC 822 date string.
     *
     * @param timestamp The timestamp in milliseconds
     * @return Formatted date string
     */
    fun format(timestamp: Long): String {
        val formatter = formatters[0].get()
        return formatter.format(Date(timestamp))
    }
}
