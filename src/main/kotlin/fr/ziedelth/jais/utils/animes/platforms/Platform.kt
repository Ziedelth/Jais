/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.platforms

import fr.ziedelth.jais.Jais
import fr.ziedelth.jais.utils.animes.*
import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.animes.countries.CountryImpl
import java.util.*

abstract class Platform {
    val checkedEpisodes = mutableListOf<String?>()
    val checkedData = mutableMapOf<String?, MutableList<String>>()

    fun addCheckEpisodes(id: String?) {
        if (!this.checkedEpisodes.contains(id) && !id.isNullOrBlank() && id != "null") this.checkedEpisodes.add(id)
    }

    fun addCheckData(id: String?, data: String) {
        val list = this.checkedData.getOrDefault(id, mutableListOf())
        list.add(data)
        this.checkedData[id] = list
    }

    protected fun addEpisode(
        title: String?,
        url: String,
        image: String,
        duration: Long,
        episodeId: String,
        list: MutableList<Episode>,
        platformImpl: PlatformImpl,
        countryImpl: CountryImpl,
        releaseDate: Calendar,
        anime: String,
        animeImage: String,
        animeGenres: Array<Genre>,
        animeDescription: String?,
        season: Long,
        number: Long,
        episodeType: EpisodeType,
        langType: LangType
    ) {
        val data = "$title$url$image$duration"
        if (this.checkedEpisodes.contains(episodeId) && this.checkedData.getOrDefault(episodeId, mutableListOf())
                .contains(data)
        ) return

        this.addCheckEpisodes(episodeId)
        this.addCheckData(episodeId, data)
        list.add(
            Episode(
                platformImpl,
                countryImpl,
                releaseDate,
                anime,
                animeImage,
                animeGenres,
                animeDescription,
                season,
                number,
                episodeType,
                langType,
                episodeId,
                title,
                url,
                image,
                duration
            )
        )
    }

    fun getPlatformImpl() = Jais.getPlatformInformation(this)
    fun getAllowedCountries(): Array<Country> = Jais.getAllowedCountries(this)
    open fun checkEpisodes(calendar: Calendar = Calendar.getInstance()): Array<Episode> = emptyArray()
    open fun checkScans(calendar: Calendar = Calendar.getInstance()): Array<Scan> = emptyArray()
}