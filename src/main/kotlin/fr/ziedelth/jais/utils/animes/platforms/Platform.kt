/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.platforms

import fr.ziedelth.jais.Jais
import fr.ziedelth.jais.utils.animes.*
import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.animes.countries.CountryHandler
import java.util.*

abstract class Platform(val jais: Jais) {
    val checkedEpisodes = mutableListOf<String?>()
    private val checkedData = mutableMapOf<String?, MutableList<String>>()

    fun addCheck(id: String?) {
        if (!this.checkedEpisodes.contains(id) && !id.isNullOrBlank() && id != "null") this.checkedEpisodes.add(id)
    }

    fun reset() {
        this.checkedEpisodes.clear()
        this.checkedData.clear()
    }

    private fun addCheckData(id: String?, data: String) {
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
        platform: Pair<PlatformHandler, Platform>,
        country: Pair<CountryHandler, Country>,
        releaseDate: Calendar,
        anime: String,
        animeImage: String?,
        animeGenres: Array<Genre>,
        animeDescription: String?,
        season: Long,
        number: Long,
        episodeType: EpisodeType,
        langType: LangType
    ) {
        val data = "$title$url$duration"
        if (this.checkedEpisodes.contains(episodeId) && this.checkedData.getOrDefault(episodeId, mutableListOf())
                .contains(data)
        ) return

        this.addCheck(episodeId)
        this.addCheckData(episodeId, data)
        list.add(
            Episode(
                platform,
                country,
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

    fun getPlatformImpl() = this.jais.getPlatformInformation(this)

    fun getAllowedCountries() = this.jais.getAllowedCountries(this)

    open fun checkEpisodes(calendar: Calendar = Calendar.getInstance()): Array<Episode> = emptyArray()

    open fun checkNews(calendar: Calendar = Calendar.getInstance()): Array<News> = emptyArray()
}