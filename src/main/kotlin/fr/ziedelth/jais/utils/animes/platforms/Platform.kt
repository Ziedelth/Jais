/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.platforms

import fr.ziedelth.jais.Jais
import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.animes.scans.Scan
import java.util.*

abstract class Platform {
    val checkedEpisodes = mutableListOf<String?>()

    fun addCheckEpisodes(id: String?) {
        if (!this.checkedEpisodes.contains(id) && !id.isNullOrBlank() && id != "null") this.checkedEpisodes.add(id)
    }

    fun getPlatformImpl() = Jais.getPlatformInformation(this)
    fun getAllowedCountries(): Array<Country> = Jais.getAllowedCountries(this)
    open fun checkEpisodes(calendar: Calendar = Calendar.getInstance()): Array<Episode> = emptyArray()
    open fun checkScans(calendar: Calendar = Calendar.getInstance()): Array<Scan> = emptyArray()
}