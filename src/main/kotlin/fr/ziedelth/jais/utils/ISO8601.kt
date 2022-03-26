/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.util.*

/* A singleton. */
object ISO8601 {
    /* Creating a `SimpleDateFormat` object with the format `yyyy-MM-dd'T'HH:mm:ssZ`. */
    private val sdf1 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

    /* This is the format that the date picker uses. */
    private val sdf2 = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)

    /* Creating a `SimpleDateFormat` object with the format `yyyy-MM-dd'T'HH:mm:ss'Z'`. */
    private val sdf3 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

    /**
     * It takes a Calendar object and returns a String.
     *
     * @param iso8601calendar The Calendar object that you want to convert to a string.
     * @return The string "2019-10-10T10:10:10:10"
     */
    fun fromCalendar(iso8601calendar: Calendar?): String {
        val date = iso8601calendar?.time
        val formatted = this.sdf1.format(date)
        return "${formatted.substring(0, 22)}:${formatted.substring(22)}"
    }

    /**
     * Convert a string in ISO8601 format to a string in a format that can be used in a date picker
     *
     * @param iso8601string The string to convert to a date.
     * @return The time in the format "HH:mm:ss"
     */
    fun fromCalendar1(iso8601string: String?): String? {
        if (iso8601string.isNullOrBlank()) return null
        val date = toCalendar1(iso8601string)?.time
        val formatted = this.sdf1.format(date)
        return "${formatted.substring(0, 22)}:${formatted.substring(22)}"
    }

    /**
     * If the string is null or blank, return null. Otherwise, convert the string to a calendar, get the time, format the
     * time, and return the formatted time
     *
     * @param iso8601string The string to convert to a date.
     * @return The date in the format of "yyyy-MM-dd HH:mm:ss"
     */
    fun fromCalendar2(iso8601string: String?): String? {
        if (iso8601string.isNullOrBlank()) return null
        val date = toCalendar2(iso8601string)?.time
        val formatted = this.sdf1.format(date)
        return "${formatted.substring(0, 22)}:${formatted.substring(22)}"
    }

    /**
     * It takes a string in ISO8601 format and converts it to a Calendar object.
     *
     * @param iso8601string The string to be converted to a Calendar.
     */
    fun toCalendar1(iso8601string: String?): Calendar? {
        if (iso8601string.isNullOrBlank()) return null
        val calendar = GregorianCalendar.getInstance()
        var s = iso8601string.replace("Z", "+00:00")
        s = "${s.substring(0, 22)}${s.substring(23)}"
        val date = this.sdf1.parse(s)
        calendar.time = date
        return calendar
    }

    /**
     * If the string is null or blank, return null. Otherwise, parse the string using the date format, and return the
     * resulting calendar
     *
     * @param iso8601string The string to parse.
     */
    fun toCalendar2(iso8601string: String?): Calendar? {
        if (iso8601string.isNullOrBlank()) return null
        val calendar = GregorianCalendar.getInstance()
        val date = this.sdf2.parse(iso8601string)
        calendar.time = date
        return calendar
    }

    /**
     * If the day of the year of the two calendars is the same, return true, otherwise return false
     *
     * @param calendar1 The first calendar object.
     * @param calendar2 The Calendar object to compare against.
     * @return The return type is a Boolean.
     */
    fun isSameDayUsingInstant(calendar1: Calendar?, calendar2: Calendar?): Boolean {
        return calendar1?.get(Calendar.DAY_OF_YEAR) == calendar2?.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Convert a string representing a date in ISO 8601 format to a string representing a date in UTC format
     *
     * @param iso8601string The string to convert to a date.
     * @return The date in UTC format.
     */
    fun toUTCDate(iso8601string: String?): String {
        this.sdf3.timeZone = TimeZone.getTimeZone("UTC")
        return this.sdf3.format(Date.from(ZonedDateTime.parse(iso8601string).toInstant()))
    }

    /**
     * It converts a date in ISO8601 format to a Calendar object.
     *
     * @param iso8601string The string to convert to a date.
     * @return The date in the local timezone.
     */
    fun fromUTCDate(iso8601string: String?): Calendar? {
        if (iso8601string.isNullOrBlank()) return null
        val calendar = Calendar.getInstance()
        this.sdf3.timeZone = TimeZone.getTimeZone("UTC")
        val date = this.sdf3.parse(toUTCDate(iso8601string))
        calendar.time = date
        return calendar
    }

    /**
     * It converts a Calendar object to a String.
     *
     * @param iso8601calendar Calendar?
     * @return The date in the format of "yyyy-MM-dd HH:mm:ss"
     */
    fun fromUTCDate(iso8601calendar: Calendar?): String {
        this.sdf3.timeZone = TimeZone.getTimeZone("UTC")
        return this.sdf3.format(Date.from(iso8601calendar?.toInstant()))
    }
}