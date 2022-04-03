/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.plugins

import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.EpisodeType
import fr.ziedelth.jais.utils.animes.Scan
import kotlin.math.floor

/* It's a function that takes a number and returns a string. */
object PluginUtils {
    /* It's a function that takes a number and returns a string. */
    fun Number.toHHMMSS() = calculateHHMMSS(this.toInt())

    /* It's a function that takes a string and returns a string. */
    fun String.onlyLettersAndDigits() = this.filter { it.isLetterOrDigit() }

    /* It's a function that takes a string and returns a string. */
    fun String.onDigits() = this.filter { it.isDigit() }

    /**
     * Calculate the hours, minutes, and seconds from a number of seconds
     *
     * @param secNum The number of seconds to convert.
     * @return a string that is the concatenation of the three variables.
     */
    private fun calculateHHMMSS(secNum: Int): String {
        val hours = floor(secNum / 3600.0)
        val minutes = floor((secNum - (hours * 3600.0)) / 60.0)
        val seconds = secNum - (hours * 3600.0) - (minutes * 60.0)
        var h: String = hours.toInt().toString()
        var m: String = minutes.toInt().toString()
        var s: String = seconds.toInt().toString()

        if (hours < 10) h = "0${hours.toInt()}"
        if (minutes < 10) m = "0${minutes.toInt()}"
        if (seconds < 10) s = "0${seconds.toInt()}"

        return "${(if (hours >= 1) "$h:" else "")}$m:$s"
    }

    /**
     * It takes an episode and returns a message that can be posted to social media
     *
     * @param episode The episode object that will be used to generate the message.
     */
    fun getMessage(episode: Episode) = "🎉 ${episode.anime}\n" +
            "${getEpisodeTitle(episode)}\n" +
            "${
                getEpisodeDataMessage(
                    episode,
                    false
                )
            } ${episode.langType.getData(episode.country.country::class.java)?.data}\n" +
            "${getEpisodeTimeMessage(episode)}\n" +
            "#Anime #Episode #${episode.platform.platformHandler.name.onlyLettersAndDigits()} #${episode.anime.onlyLettersAndDigits()}"

    /**
     * It takes an episode and returns a string
     *
     * @param episode The episode object to get the message for.
     */
    fun getMarkdownMessage(episode: Episode) = "**${getEpisodeTitle(episode)}**\n" +
            getEpisodeDataMessage(episode) +
            getEpisodeTimeMessage(episode)

    /**
     * `Get the title of an episode, or "＞﹏＜" if it's null`
     *
     * @param episode The episode object that we want to get the title of.
     */
    private fun getEpisodeTitle(episode: Episode) = episode.title ?: "＞﹏＜"

    /**
     * `Get the time of an episode in HH:MM:SS format.`
     *
     * @param episode The episode to get the time message for.
     */
    private fun getEpisodeTimeMessage(episode: Episode) =
        "🎬 ${if (episode.duration != -1L) episode.duration.toHHMMSS() else "??:??"}"

    /**
     * It takes an episode and returns a string that contains the episode's country, season, episode type, and episode
     * number
     *
     * @param episode The episode to get the data for.
     * @param newLine Boolean = true
     */
    private fun getEpisodeDataMessage(episode: Episode, newLine: Boolean = true) =
        "${episode.country.countryHandler.season} ${episode.season} • ${episode.episodeType.getData(episode.country.country::class.java)?.data}${if (episode.episodeType != EpisodeType.EPISODE) "" else " ${episode.number}"}${if (newLine) "\n" else ""}"

    /**
     * It takes a scan object and returns a string that contains the scan data and the hashtag
     *
     * @param scan The scan object that was just scanned.
     */
    fun getMessage(scan: Scan) = "🎉 ${scan.anime}\n" +
            "${
                getScanDataMessage(
                    scan,
                    false
                )
            }\n" +
            "#Anime #Scan #${scan.platform.platformHandler.name.onlyLettersAndDigits()} #${scan.anime.onlyLettersAndDigits()}"

    /**
     * It returns a string that contains the scan data.
     *
     * @param scan Scan
     */
    fun getMarkdownMessage(scan: Scan) = getScanDataMessage(scan)

    /**
     * It takes a Scan object and returns a String
     *
     * @param scan Scan
     * @param newLine Boolean = true
     */
    private fun getScanDataMessage(scan: Scan, newLine: Boolean = true) =
        "${scan.episodeType.getData(scan.country.country::class.java)?.data} ${scan.number}${if (newLine) "\n" else ""}"
}