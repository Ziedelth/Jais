/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.platforms

import fr.ziedelth.jais.Jais
import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.animes.episodes.Episode
import java.util.*

abstract class Platform {
    fun getAllowedCountries(): Array<Country> = Jais.getAllowedCountries(this)
    abstract fun checkLastNews()
    abstract fun checkEpisodes(calendar: Calendar = Calendar.getInstance()): Array<Episode>
}