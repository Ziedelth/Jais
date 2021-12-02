/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.util.*

object ISO8601 {
    private val sdf1 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
    private val sdf2 = SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z", Locale.ENGLISH)
    private val sdf3 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    private val sdf4 = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)

    fun fromCalendar(iso8601calendar: Calendar?): String {
        val date = iso8601calendar?.time
        val formatted = this.sdf1.format(date)
        return "${formatted.substring(0, 22)}:${formatted.substring(22)}"
    }

    fun fromCalendar1(iso8601string: String?): String? {
        if (iso8601string.isNullOrBlank()) return null
        val date = toCalendar1(iso8601string)?.time
        val formatted = this.sdf1.format(date)
        return "${formatted.substring(0, 22)}:${formatted.substring(22)}"
    }

    fun fromCalendar2(iso8601string: String?): String? {
        if (iso8601string.isNullOrBlank()) return null
        val date = toCalendar2(iso8601string)?.time
        val formatted = this.sdf1.format(date)
        return "${formatted.substring(0, 22)}:${formatted.substring(22)}"
    }

    fun fromCalendar4(iso8601string: String?): String? {
        if (iso8601string.isNullOrBlank()) return null
        val date = toCalendar4(iso8601string)?.time
        val formatted = this.sdf1.format(date)
        return "${formatted.substring(0, 22)}:${formatted.substring(22)}"
    }

    fun toCalendar1(iso8601string: String?): Calendar? {
        if (iso8601string.isNullOrBlank()) return null
        val calendar = GregorianCalendar.getInstance()
        var s = iso8601string.replace("Z", "+00:00")
        s = "${s.substring(0, 22)}${s.substring(23)}"
        val date = this.sdf1.parse(s)
        calendar.time = date
        return calendar
    }

    fun toCalendar2(iso8601string: String?): Calendar? {
        if (iso8601string.isNullOrBlank()) return null
        val calendar = GregorianCalendar.getInstance()
        val date = this.sdf2.parse(iso8601string)
        calendar.time = date
        return calendar
    }

    fun toCalendar4(iso8601string: String?): Calendar? {
        if (iso8601string.isNullOrBlank()) return null
        val calendar = GregorianCalendar.getInstance()
        val date = this.sdf4.parse(iso8601string)
        calendar.time = date
        return calendar
    }

    fun isSameDayUsingInstant(calendar1: Calendar?, calendar2: Calendar?): Boolean {
        return calendar1?.get(Calendar.DAY_OF_YEAR) == calendar2?.get(Calendar.DAY_OF_YEAR)
    }

    fun isSameDayUsingISO8601(iso8601string1: String?, iso8601string2: String?): Boolean {
        return iso8601string1?.split("T")?.get(0) == iso8601string2?.split("T")?.get(0)
    }

    fun toUTCDate(iso8601string: String?): String {
        this.sdf3.timeZone = TimeZone.getTimeZone("UTC")
        return this.sdf3.format(Date.from(ZonedDateTime.parse(iso8601string).toInstant()))
    }

    fun fromUTCDate(iso8601string: String?): Calendar? {
        if (iso8601string.isNullOrBlank()) return null
        val calendar = Calendar.getInstance()
        this.sdf3.timeZone = TimeZone.getTimeZone("UTC")
        val date = this.sdf3.parse(toUTCDate(iso8601string))
        calendar.time = date
        return calendar
    }

    fun fromUTCDate(iso8601calendar: Calendar?): String {
        this.sdf3.timeZone = TimeZone.getTimeZone("UTC")
        return this.sdf3.format(Date.from(iso8601calendar?.toInstant()))
    }
}