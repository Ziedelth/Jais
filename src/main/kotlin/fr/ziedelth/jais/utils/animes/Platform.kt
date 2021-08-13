package fr.ziedelth.jais.utils.animes

import java.awt.Color

interface Platform {
    fun getName(): String
    fun getURL(): String
    fun getImage(): String
    fun getColor(): Color
    fun getAllowedCountries(): Array<Country> = arrayOf()
    fun getLastNews(): Array<News> = arrayOf()
    fun getLastEpisodes(): Array<Episode> = arrayOf()
}