/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import java.text.SimpleDateFormat
import java.time.temporal.ChronoUnit
import java.util.*


object ISO8601 {
    private val sdf1 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
    private val sdf2 = SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z", Locale.ENGLISH)

    private fun fromCalendar(calendar: Calendar?): String? {
        if (calendar == null) return null
        val date = calendar.time
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

    fun now(): String = fromCalendar(GregorianCalendar.getInstance())!!

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

    fun isSameDayUsingInstant(calendar1: Calendar?, calendar2: Calendar?): Boolean {
        val instant1 = calendar1?.toInstant()?.truncatedTo(ChronoUnit.DAYS)
        val instant2 = calendar2?.toInstant()?.truncatedTo(ChronoUnit.DAYS)
        return instant1 == instant2
    }
}