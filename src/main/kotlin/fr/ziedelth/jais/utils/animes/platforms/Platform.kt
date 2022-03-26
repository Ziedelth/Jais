/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.platforms

import fr.ziedelth.jais.Jais
import fr.ziedelth.jais.utils.animes.*
import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.animes.countries.CountryImpl
import java.util.*

abstract class Platform(val jais: Jais) {
    val checkedEpisodes = mutableListOf<String?>()
    private val checkedData = mutableMapOf<String?, MutableList<String>>()

    /**
     * If the episode is not already in the list of checked episodes, and the episode id is not null or blank, add the
     * episode id to the list of checked episodes
     *
     * @param id The id of the episode you want to check.
     */
    fun addCheck(id: String?) {
        if (!this.checkedEpisodes.contains(id) && !id.isNullOrBlank() && id != "null") this.checkedEpisodes.add(id)
    }

    /**
     * It clears the checkedEpisodes and checkedData lists
     */
    fun reset() {
        this.checkedEpisodes.clear()
        this.checkedData.clear()
    }

    /**
     * Add the data to the list of checked data for the given id
     *
     * @param id The id of the checkbox.
     * @param data The data to be added to the checkedData map.
     */
    private fun addCheckData(id: String?, data: String) {
        val list = this.checkedData.getOrDefault(id, mutableListOf())
        list.add(data)
        this.checkedData[id] = list
    }

    /**
     * It adds an episode to the list if it's not already in the list
     *
     * @param title The title of the episode.
     * @param url The url of the episode.
     * @param image The image of the episode.
     * @param duration The duration of the episode in milliseconds.
     * @param episodeId The episode ID.
     * @param list MutableList<Episode>
     * @param platformImpl PlatformImpl,
     * @param countryImpl CountryImpl,
     * @param releaseDate Calendar
     * @param anime The name of the anime.
     * @param animeImage The image of the anime.
     * @param animeGenres Array<Genre>
     * @param animeDescription String?
     * @param season The season number.
     * @param number The episode number.
     * @param episodeType The type of episode.
     * @param langType LangType
     * @return Nothing.
     */
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

    /**
     * Get the platform information for the current platform
     */
    fun getPlatformImpl() = this.jais.getPlatformInformation(this)

    /**
     * Get the allowed countries for this user
     */
    fun getAllowedCountries(): Array<Country> = this.jais.getAllowedCountries(this)

    /**
     * "Check the episodes for the current day."
     *
     * The function is open, which means that it can be overridden in a subclass
     *
     * @param calendar The calendar to use for the episode check.
     */
    open fun checkEpisodes(calendar: Calendar = Calendar.getInstance()): Array<Episode> = emptyArray()

    /**
     * "Get all scans from the database that occurred on the current day."
     *
     * The function is open, which means that it can be overridden in a subclass
     *
     * @param calendar The calendar to use for the date calculations.
     */
    open fun checkScans(calendar: Calendar = Calendar.getInstance()): Array<Scan> = emptyArray()

    /**
     * "Get the news for the current day."
     *
     * The function is open, which means that it can be overridden in a subclass
     *
     * @param calendar Calendar = Calendar.getInstance()
     */
    open fun checkNews(calendar: Calendar = Calendar.getInstance()): Array<News> = emptyArray()
}