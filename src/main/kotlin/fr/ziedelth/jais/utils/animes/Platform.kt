/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes

import java.awt.Color
import java.text.SimpleDateFormat
import java.util.*

interface Platform {
    fun getName(): String
    fun getURL(): String
    fun getImage(): String
    fun getColor(): Color
    fun getAllowedCountries(): Array<Country> = arrayOf()
    fun getLastNews(): Array<News> = arrayOf()
    fun getLastEpisodes(): Array<Episode> = arrayOf()

    fun getFrenchDate(calendar: Calendar = Calendar.getInstance()): String {
        return SimpleDateFormat("dd-MM-yyyy").format(calendar.time)
    }

    fun getUniversalDate(calendar: Calendar = Calendar.getInstance()): String {
        return SimpleDateFormat("yyyy-MM-dd").format(calendar.time)
    }
}