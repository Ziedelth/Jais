package fr.ziedelth.ziedbot.utils.animes

import java.awt.Color
import java.text.SimpleDateFormat
import java.util.*

interface Platform {
    fun getName(): String
    fun getURL(): String
    fun getImage(): String
    fun getColor(): Color
    fun getLastNews(): Array<News>
    fun getLastEpisodes(): Array<Episode>

    fun toStringCalendar(calendar: Calendar): String =
        SimpleDateFormat("HH:mm:ss yyyy/MM/dd", Locale.FRANCE).format(Date.from(calendar.toInstant()))
}