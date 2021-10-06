/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import java.text.SimpleDateFormat
import java.util.*


object ISO8601 {
    private val sdf1 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
    private val sdf2 = SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z", Locale.ENGLISH)

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
}