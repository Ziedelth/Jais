/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.platforms

import fr.ziedelth.jais.Jais
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.animes.episodes.Episode
import java.util.*

abstract class Platform {
    private var lastDate = Calendar.getInstance()
    protected val checkedEpisodes = mutableListOf<String?>()

    fun addCheckEpisodes(id: String?) {
        if (!ISO8601.isSameDayUsingInstant(Calendar.getInstance(), this.lastDate)) this.checkedEpisodes.clear()
        if (!this.checkedEpisodes.contains(id) && !id.isNullOrBlank() && id != "null") this.checkedEpisodes.add(id)
    }

    fun getAllowedCountries(): Array<Country> = Jais.getAllowedCountries(this)
    abstract fun checkEpisodes(calendar: Calendar = Calendar.getInstance()): Array<Episode>
}