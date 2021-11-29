/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.plugins

import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.EpisodeType
import fr.ziedelth.jais.utils.animes.scans.Scan
import kotlin.math.floor

object PluginUtils {
    fun Number.toHHMMSS() = calculateHHMMSS(this.toInt())
    fun String.onlyLettersAndDigits() = this.filter { it.isLetterOrDigit() }

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

    fun getMarkdownMessage(episode: Episode) = "**${getEpisodeTitle(episode)}**\n" +
            getEpisodeDataMessage(episode) +
            getEpisodeTimeMessage(episode)

    private fun getEpisodeTitle(episode: Episode) = episode.title ?: "＞﹏＜"

    private fun getEpisodeTimeMessage(episode: Episode) =
        "🎬 ${if (episode.duration != -1L) episode.duration.toHHMMSS() else "??:??"}"

    private fun getEpisodeDataMessage(episode: Episode, newLine: Boolean = true) =
        "${episode.country.countryHandler.season} ${episode.season} • ${episode.episodeType.getData(episode.country.country::class.java)?.data}${if (episode.episodeType != EpisodeType.EPISODE) "" else " ${episode.number}"}${if (newLine) "\n" else ""}"

    fun getMessage(scan: Scan) = "🎉 ${scan.anime}\n" +
            "${
                getScanDataMessage(
                    scan,
                    false
                )
            }\n" +
            "#Anime #Scan #${scan.platform.platformHandler.name.onlyLettersAndDigits()} #${scan.anime.onlyLettersAndDigits()}"

    fun getMarkdownMessage(scan: Scan) = getScanDataMessage(scan)

    private fun getScanDataMessage(scan: Scan, newLine: Boolean = true) =
        "${scan.episodeType.getData(scan.country.country::class.java)?.data} ${scan.number}${if (newLine) "\n" else ""}"
}