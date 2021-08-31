/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import java.text.SimpleDateFormat
import java.util.*

object ISO8601 {
    private val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

    fun fromCalendar(calendar: Calendar): String {
        val date = calendar.time
        val formatted = this.sdf.format(date)
        return "${formatted.substring(0, 22)}:${formatted.substring(22)}"
    }

    fun now(): String = fromCalendar(GregorianCalendar.getInstance())

    fun toCalendar(iso8601string: String): Calendar {
        val calendar = GregorianCalendar.getInstance()
        var s = iso8601string.replace("Z", "+00:00")
        s = "${s.substring(0, 22)}${s.substring(23)}"
        val date = this.sdf.parse(s)
        calendar.time = date
        return calendar
    }
}